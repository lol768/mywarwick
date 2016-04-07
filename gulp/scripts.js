var paths = global.paths;

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

var babelify = require('babelify');
var browserify = require('browserify');
var browserifyInc = require('browserify-incremental');
var browserifyShim = require('browserify-shim');
var envify = require('loose-envify/custom');
var eslint = require('gulp-eslint');
var filenames = require('gulp-filenames');
var insert = require('gulp-insert');
var less = require('gulp-less');
var manifest = require('gulp-manifest');
var rename = require('gulp-rename');
var swagger = require('gulp-swagger');
var swPrecache = require('sw-precache');
var uglify = require('gulp-uglify');
var watchify = require('watchify');
var merge = require('merge-stream');

var bundleEvents = require('./events');

var browserifyOptions = function (cacheName, entries) {
  return {
    entries: entries,
    basedir: 'app/assets/js',
    cacheFile: './browserify-cache-'+cacheName+'.json',
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

var browserifyFlags = function (b) {
  b.exclude('jquery');
  b.transform({ global: true }, browserifyShim);
  return b;
};

// Function for running Browserify on JS, since
// we reuse it a couple of times.
var bundle = function (browserify, outputFile) {
  return browserify.bundle()
    .on('error', function (e) {
      gutil.log(gutil.colors.red(e.stack));
    })
    .pipe(mold.transformSourcesRelativeTo(path.join(__dirname, '..', 'app', 'assets', 'js')))
    .pipe(source(outputFile))
    .pipe(buffer())
    .pipe(sourcemaps.init({ loadMaps: true }))
    .pipe(replace('$$BUILDTIME$$', (new Date()).toString()))
    .pipe(UGLIFY ? uglify() : gutil.noop())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.scriptOut));
};



var SCRIPTS = {
  // Mapping from bundle name to entry point
  'bundle.js': 'main.js'
};

function cacheName(name) {
  return name.replace('.js','');
}

gulp.task('scripts', [], function () {
  return merge(_.map(SCRIPTS, function (entries, output) {
    var b = browserifyInc(browserifyOptions(cacheName(output), entries));
    browserifyFlags(b);
    return bundle(b, output);
  }));
});

// Recompile scripts on changes. Watchify is more efficient than
// grunt.watch as it knows how to do incremental rebuilds.
gulp.task('watch-scripts', [], function () {
  return merge(_.map(SCRIPTS, function (entries, output) {
    var opts = _.extend({}, watchify.args, browserifyOptions(cacheName(output), entries));
    var bw = watchify(browserify(opts));
    browserifyFlags(bw);
    bw.on('update', function () {
      gutil.log("Browserify update");
      bundleEvents.emit('scripts-updating');
      bundle(bw, output).on('end', function () {
        gutil.log("Browserify updated");
        bundleEvents.emit('scripts-updated');
      });
    });
    bw.on('log', gutil.log);
    return bundle(bw, output);
  }));
});

gulp.task('lint', function () {
  return gulp.src([paths.assetPath + '/js/**/*.js'])
    .pipe(eslint('.eslintrc.json'))
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

function generateServiceWorker(watch) {
  var swPrecache = require('sw-precache');
  var jsBundle = ['target/gulp/js/bundle.js'];

  return swPrecache.generate({
      cacheId: 'start',
      handleFetch: OFFLINE_WORKERS,
      staticFileGlobs: [
        'public/**/*',
        paths.assetsOut + '/**/!(*.map|appcache.manifest)',
      ],
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
      },
      maximumFileSizeToCacheInBytes: 3 * 1024 * 1024
    })
    .then(function (offlineWorker) {
      return browserifyFlags(browserifyInc(browserifyOptions(cacheName('push-worker'), 'push-worker.js'))).bundle()
        .on('error', function (e) {
          gutil.log(gutil.colors.red(e.toString()));
        })
        .pipe(source('service-worker.js'))
        .pipe(buffer())
        .pipe(insert.prepend(offlineWorker))
        .pipe(UGLIFY ? uglify() : gutil.noop())
        .pipe(gulp.dest(paths.assetsOut));
    });
}

// Not strictly a script thing, but here it is in scripts.js.
function generateManifest() {
  if (OFFLINE_WORKERS) {
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
            cache: [
              '/',
              '/activity',
              '/notifications',
              '/news',
              '/search',
              '/assets/images/no-photo.png'
            ],
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
}

gulp.task('service-worker', ['all-static'], generateServiceWorker);
// Run once the scripts and styles are in place
gulp.task('manifest', ['all-static'], generateManifest);

/* The other watchers have been set up to emit some events that we can listen to */

gulp.task('watch-service-worker', ['watch-styles','watch-scripts'], function () {
  //gulp.watch(paths.assetPath + '/css/**', generateServiceWorker);
  bundleEvents.on('styles-updated', generateServiceWorker);
  bundleEvents.on('scripts-updated', generateServiceWorker);
});

gulp.task('watch-manifest', ['watch-styles','watch-scripts'], function () {
  //gulp.watch(paths.assetPath + '/css/**', ['manifest']);
  bundleEvents.on('styles-updated', generateManifest);
  bundleEvents.on('scripts-updated', generateManifest);
});

// Get the current FA version for use in the cache manifest
function getFontAwesomeVersion() {
  return new Promise(function (resolve, reject) {
    fs.readFile('node_modules/id7/less/font-awesome/variables.less', function(e, data) {
      if (e) {
        reject(e);
      } else {
        var match = data.toString().match(/fa-version:\s+"(.+)";/);
        if (match) {
          resolve(match[1]);
        } else {
          reject('Pattern not found');
        }
      }
    });
  });
}
