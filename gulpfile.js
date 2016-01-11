/*
 * Build file for JS and CSS assets.
 *
 *     npm install
 *     node_modules/.bin/gulp watch-assets
 */
'use strict';

var gulp = require('gulp');
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

var lessCompiler = require('less');

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

var browserifyOptions = {
  entries: 'main.js',
  basedir: 'app/assets/js',
  debug: true, // confusingly, this enables sourcemaps
  transform: [babelify] // Transforms ES6 + JSX into normal JS
};

var PRODUCTION = (process.env.PRODUCTION !== 'false');
if (PRODUCTION) {
  gutil.log(gutil.colors.yellow('Production build (use PRODUCTION=false in development).'));
} else {
  gutil.log(gutil.colors.yellow('Development build.'));
}

// Function for running Browserify on JS, since
// we reuse it a couple of times.
var bundle = function (browserify, outputFile) {
  browserify.bundle()
    .on('error', function (e) {
      gutil.log(gutil.colors.red(e.toString()));
    })
    .pipe(mold.transformSourcesRelativeTo(path.join(__dirname, 'app', 'assets', 'js')))
    .pipe(source(outputFile))
    .pipe(buffer())
    .pipe(sourcemaps.init({loadMaps: true}))
    .pipe(replace('$$BUILDTIME$$', (new Date()).toString()))
    .pipe(PRODUCTION ? uglify() : gutil.noop())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.scriptOut))
};

var browserifyFlags = function(b) {
  b.exclude('jquery');
  b.transform({global:true}, browserifyShim);
  return b;
}

gulp.task('scripts', [], function () {
  var mainBundle = browserifyFlags(browserify(browserifyOptions));
  bundle(mainBundle, 'bundle.js');

  var serviceWorker = browserifyFlags(browserify(Object.assign({}, browserifyOptions, {entries: 'service-worker-main.js'})));
  bundle(serviceWorker, 'service-worker.js');
});

// Recompile scripts on changes. Watchify is more efficient than
// grunt.watch as it knows how to do incremental rebuilds.
gulp.task('watch-scripts', [], function () {
  var bw = watchify(browserifyFlags(browserify(_.assign({}, watchify.args, browserifyOptions))));
  bw.on('update', function () {
    bundle(bw, 'bundle.js');
  });
  bw.on('log', gutil.log);
  return bundle(bw, 'bundle.js');
});

/**
 * Copies static resources out of an NPM module, and into
 * the asset output directory.
 */
function exportAssetModule(name, taskName, baseDir, extraExtensions) {
  gulp.task(taskName, function () {
    var base = 'node_modules/' + name + '/' + baseDir;

    var baseExtensions = ['otf', 'eot', 'woff', 'woff2', 'ttf', 'js', 'js.map', 'gif', 'png', 'jpg', 'svg'];
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

// Run once the scripts and styles are in place
gulp.task('manifest', ['scripts', 'styles'], function () {
  if (PRODUCTION) {
    getFontAwesomeVersion(function (fontAwesomeVersion) {
      return gulp.src([
        paths.assetsOut + '/**/*',
        '!' + paths.assetsOut + '/**/*.map' // don't cache source maps
      ], {base: path.assetsOut})
        .pipe(rename(function (path) {
          if (path.basename == 'fontawesome-webfont') {
            path.extname += '?v=' + fontAwesomeVersion;
          }

          return path;
        }))
        .pipe(manifest({
          cache: ['/', '/activity', '/notifications', '/news', '/search'],
          hash: true,
          exclude: 'app.manifest',
          prefix: '/assets/'
        }))
        .pipe(gulp.dest(paths.assetsOut));
    });
  } else {
    // Produce an empty manifest file
    gulp.src([])
      .pipe(manifest())
      .pipe(gulp.dest(paths.assetsOut));
  }
});

// Shortcuts for building all asset types at once
gulp.task('assets', ['scripts', 'styles', 'manifest']);
gulp.task('watch-assets', ['watch-scripts', 'watch-styles']);
gulp.task('wizard', ['watch-assets']);

// Get the current FA version for use in the cache manifest
function getFontAwesomeVersion(cb) {
  lessCompiler.render('@import "node_modules/id7/less/font-awesome/variables.less"; @{fa-version} { a: a; }', {},
    function (e, output) {
      var version = output.css.match(/"([0-9\.]+)"/)[1];
      cb(version);
    });
}
