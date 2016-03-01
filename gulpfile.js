/*
 * Build file for JS and CSS assets.
 *
 *     npm install
 *     node_modules/.bin/gulp watch-assets
 */
'use strict';

var gulp = require('gulp');
var fs = require('fs');
var gutil = require('gulp-util');
var sourcemaps = require('gulp-sourcemaps');
var replace = require('gulp-replace');
var source = require('vinyl-source-stream');
var buffer = require('vinyl-buffer');
var _ = require('lodash');
var path = require('path');
var mold = require('mold-source-map');

var postcss = require('gulp-postcss');
var less = require('gulp-less');
var uglify = require('gulp-uglify');
var browserify = require('browserify');
var browserifyShim = require('browserify-shim');
var babelify = require('babelify');
var watchify = require('watchify');
var autoprefix = require('autoprefixer-core');
var manifest = require('gulp-manifest');
var rename = require('gulp-rename');
var filenames = require('gulp-filenames');
var insert = require('gulp-insert');
var swPrecache = require('sw-precache');
var eslint = require('gulp-eslint');
var envify = require('loose-envify/custom');

var lessCompiler = require('less');

gulpOpts = {env:{}};
try {
  fs.accessSync('./gulpopts.json', fs.R_OK);
  var gulpOpts = require('./gulpopts.json');
  gutil.log('Got opts');
} catch (e) {
  gutil.log(gutil.colors.yellow('No gulpopts.json'));
}
function option(name) {
  return process.env[name] || gulpOpts.env[name];
}


var paths = {
  assetPath: 'app/assets',

  scriptOut: 'target/gulp/js',

  assetsOut: 'target/gulp',

  styleIn: ['app/assets/css/main.less'],
  styleOut: 'target/gulp/css',

  // Paths under node_modules that will be searched when @import-ing in your LESS.
  styleModules: [
    'id7/less'
  ]
};

var PRODUCTION = ((''+option('PRODUCTION')) !== 'false');
if (PRODUCTION) {
  gutil.log(gutil.colors.yellow('Production build (use PRODUCTION=false in development).'));
} else {
  gutil.log(gutil.colors.yellow('Development build.'));
}

var browserifyOptions = function (entries) {
  return {
    entries: entries,
    basedir: 'app/assets/js',
    debug: true, // confusingly, this enables sourcemaps
    transform: [
      babelify,
      [
        envify({
          _: 'purge',
          NODE_ENV: PRODUCTION ? 'production' : 'development'
        }),
        {
          global: true
        }
      ]
    ]
  }
};

// Function for running Browserify on JS, since
// we reuse it a couple of times.
var bundle = function (browserify, outputFile) {
  return browserify.bundle()
    .on('error', function (e) {
      gutil.log(gutil.colors.red(e.stack));
    })
    .pipe(mold.transformSourcesRelativeTo(path.join(__dirname, 'app', 'assets', 'js')))
    .pipe(source(outputFile))
    .pipe(buffer())
    .pipe(sourcemaps.init({loadMaps: true}))
    .pipe(replace('$$BUILDTIME$$', (new Date()).toString()))
    .pipe(PRODUCTION ? uglify() : gutil.noop())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.scriptOut));
};

var browserifyFlags = function (b) {
  b.exclude('jquery');
  b.transform({global: true}, browserifyShim);
  return b;
};

var SCRIPTS = {
  // Mapping from bundle name to entry point
  'bundle.js': 'main.js'
};

gulp.task('scripts', [], function () {
  return _.map(SCRIPTS, function (entries, output) {
    var b = browserifyFlags(browserify(browserifyOptions(entries)));
    return bundle(b, output);
  });
});

// Recompile scripts on changes. Watchify is more efficient than
// grunt.watch as it knows how to do incremental rebuilds.
gulp.task('watch-scripts', [], function () {
  return _.map(SCRIPTS, function (entries, output) {
    var bw = watchify(browserifyFlags(browserify(_.assign({}, watchify.args, browserifyOptions(entries)))));
    bw.on('update', function () {
      bundle(bw, output);
    });
    bw.on('log', gutil.log);
    return bundle(bw, output);
  });
});

/**
 * Copies static resources out of an NPM module, and into
 * the asset output directory.
 */
function exportAssetModule(name, taskName, baseDir, extraExtensions) {
  gulp.task(taskName, function () {
    var base = 'node_modules/' + name + '/' + baseDir;

    var baseExtensions = ['otf', 'eot', 'woff', 'woff2', 'ttf', 'js', 'js.map', 'gif', 'png', 'jpg', 'svg', 'ico'];
    var srcs = (extraExtensions || []).concat(baseExtensions);
    var srcPaths = srcs.map(function (s) {
      return base + '/**/*.' + s;
    });

    return gulp.src(srcPaths, {base: base})
      .pipe(gulp.dest(paths.assetsOut + '/lib/' + name))
  });
}

exportAssetModule('id7', 'id7-static', 'dist');

gulp.task('styles', ['id7-static'], function () {
  return gulp.src(paths.styleIn)
    .pipe(sourcemaps.init())
    .pipe(less({
      // Allow requiring less relative to node_modules, plus any other dir under node_modules
      // that's in styleModules.
      paths: [path.join(__dirname, 'node_modules')].concat(paths.styleModules.map(function (modulePath) {
        return path.join(__dirname, 'node_modules', modulePath)
      }))
    }))
    .pipe(postcss([
      autoprefix({browsers: 'last 1 version'})
    ]))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.styleOut))
});

// Recompile LESS on changes
gulp.task('watch-styles', ['styles'], function () {
  return gulp.watch(paths.assetPath + '/css/**/*.less', ['styles']);
});

gulp.task('pre-service-worker', ['scripts', 'styles'], function () {
  return gulp.src([
      'public/**/*',
      paths.assetsOut + '/**/*',
      '!' + paths.assetsOut + '/**/*.map', // don't cache source maps
      '!' + paths.assetsOut + '/appcache.manifest' // don't cache appcache manifest
    ], {
      base: './'
    })
    .pipe(filenames('offline-cache'));
});

gulp.task('service-worker', ['pre-service-worker'], function () {
  var jsBundle = ['target/gulp/js/bundle.js'];

  return swPrecache.generate({
      cacheId: 'start',
      handleFetch: PRODUCTION,
      staticFileGlobs: filenames.get('offline-cache'),
      stripPrefixRegex: '(target/gulp|public)',
      replacePrefix: '/assets',
      ignoreUrlParametersMatching: [/^v$/],
      logger: gutil.log,
      dynamicUrlToDependencies: {
        '/': jsBundle,
        '/notifications': jsBundle,
        '/activity': jsBundle,
        '/news': jsBundle,
        '/search': jsBundle
      }
    })
    .then(function (offlineWorker) {
      return browserifyFlags(browserify(browserifyOptions('push-worker.js'))).bundle()
        .on('error', function (e) {
          gutil.log(gutil.colors.red(e.toString()));
        })
        .pipe(source('service-worker.js'))
        .pipe(buffer())
        .pipe(insert.prepend(offlineWorker))
        .pipe(uglify())
        .pipe(gulp.dest(paths.assetsOut));
    });
});

// Run once the scripts and styles are in place
gulp.task('manifest', ['scripts', 'styles'], function () {
  if (PRODUCTION) {
    var cacheableAssets = gulp.src([
      paths.assetsOut + '/**/*',
      '!' + paths.assetsOut + '/**/*.map', // don't cache source maps
      '!' + paths.assetsOut + '/**/*-worker.js' // don't cache service workers
    ], {
      base: paths.assetsOut
    });

    return getFontAwesomeVersion()
      .then(function (fontAwesomeVersion) {
        return cacheableAssets
          .pipe(rename(function (path) {
            if (path.basename == 'fontawesome-webfont') {
              path.extname += '?v=' + fontAwesomeVersion;
            }

            return path;
          }))
          .pipe(manifest({
            cache: ['/', '/activity', '/notifications', '/news', '/search'],
            hash: true,
            exclude: 'appcache.manifest',
            prefix: '/assets/',
            filename: 'appcache.manifest'
          }))
          .pipe(gulp.dest(paths.assetsOut));
      });
  } else {
    // Produce an empty manifest file
    return gulp.src([])
      .pipe(manifest({
        filename: 'appcache.manifest'
      }))
      .pipe(gulp.dest(paths.assetsOut));
  }
});

gulp.task('lint', function () {
  return gulp.src([paths.assetPath + '/js/**/*.js'])
    .pipe(eslint('.eslintrc.json'))
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

// Shortcuts for building all asset types at once
gulp.task('assets', ['lint', 'scripts', 'styles', 'manifest', 'service-worker']);
gulp.task('watch-assets', ['watch-scripts', 'watch-styles']);
gulp.task('wizard', ['watch-assets']);

// Get the current FA version for use in the cache manifest
function getFontAwesomeVersion() {
  return new Promise(function (resolve, reject) {
    lessCompiler.render('@import "node_modules/id7/less/font-awesome/variables.less"; @{fa-version} { a: a; }', {},
      function (e, output) {
        var version = output.css.match(/"([0-9\.]+)"/)[1];
        if (version)
          resolve(version);
        else
          reject();
      });
  });
}
