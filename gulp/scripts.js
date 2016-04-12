'use strict';

const gulp = require('gulp');
const fs = require('fs');
const gutil = require('gulp-util');
const sourcemaps = require('gulp-sourcemaps');
const replace = require('gulp-replace');
const source = require('vinyl-source-stream');
const buffer = require('vinyl-buffer');
const _ = require('lodash');
const path = require('path');
const mold = require('mold-source-map');

const babelify = require('babelify');
const browserify = require('browserify');
const browserifyInc = require('browserify-incremental');
const browserifyShim = require('browserify-shim');
const envify = require('loose-envify/custom');
const eslint = require('gulp-eslint');
const insert = require('gulp-insert');
const manifest = require('gulp-manifest');
const rename = require('gulp-rename');
const uglify = require('gulp-uglify');
const watchify = require('watchify');
const merge = require('merge-stream');

const bundleEvents = require('./events');

function browserifyOptions(cacheName, entries) {
  return {
    entries: entries,
    basedir: 'app/assets/js',
    cacheFile: `./target/browserify-cache-${cacheName}.json`,
    debug: true, // confusingly, this enables sourcemaps
    transform: [
      babelify,
      [
        envify({ _: 'purge', NODE_ENV: PRODUCTION ? 'production' : 'development' }),
        { global: true },
      ],
      [browserifyShim, { global: true }],
    ],
    // Excluded from bundle, then browserify-shim hooks up require('jquery') to point
    // at the global version.
    // TODO use standalone id7 js and be masters of our own jquery
    excludes: ['jquery'],
  };
}

function createBrowserify(options) {
  var factory = browserifyInc;
  if (options.incremental === false) {
    factory = browserify;
  }
  const b = factory(options);
  (options.excludes || []).forEach((el) => b.exclude(el));
  return b;
}

// Function for running Browserify on JS, since
// we reuse it a couple of times.
function bundle(b, outputFile) {
  return b.bundle()
    .on('error', (e) => {
      gutil.log(gutil.colors.red(e.stack));
    })
    .pipe(mold.transformSourcesRelativeTo(path.join(__dirname, '..', 'app', 'assets', 'js')))
    .pipe(source(outputFile))
    .pipe(buffer())
    .pipe(sourcemaps.init({ loadMaps: true }))
      .pipe(UGLIFY ? uglify() : gutil.noop())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.scriptOut));
}

const SCRIPTS = {
  // Mapping from bundle name to entry point
  'bundle.js': 'main.js',
};

function cacheName(name) {
  return name.replace('.js', '');
}

gulp.task('scripts', [], () =>
  merge(_.map(SCRIPTS, (entries, output) => {
    const bopts = browserifyOptions(cacheName(output), entries);
    const b = createBrowserify(bopts);
    return bundle(b, output);
  }))
);

// Recompile scripts on changes. Watchify is more efficient than
// grunt.watch as it knows how to do incremental rebuilds.
gulp.task('watch-scripts', [], () =>
  merge(_.map(SCRIPTS, (entries, output) => {
    // incremental doesn't play nice with watchify, so disable
    const bopts = _.extend({ incremental: false }, watchify.args, browserifyOptions(cacheName(output), entries));
    const b = watchify(createBrowserify(bopts));
    b.on('update', () => {
      bundleEvents.emit('scripts-updating');
      bundle(b, output).on('end', () => {
        bundleEvents.emit('scripts-updated');
      });
    });
    b.on('log', gutil.log);
    return bundle(b, output);
  }))
);

gulp.task('lint', () => {
  return gulp.src([`${paths.assetPath}/js/**/*.js`])
    .pipe(eslint('.eslintrc.json'))
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

function generateServiceWorker(watch) {
  const swPrecache = require('sw-precache');
  const jsBundle = ['target/gulp/js/bundle.js','app/assets/js/push-worker.js'];

  return swPrecache.generate({
    cacheId: 'start',
    handleFetch: OFFLINE_WORKERS,
    staticFileGlobs: [
      'public/**/*',
      `${paths.assetsOut}/**/!(*.map|appcache.manifest|*-worker.js|swagger.json)`,
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
      '/search': jsBundle,
    },
    maximumFileSizeToCacheInBytes: 3 * 1024 * 1024,
  })
  .then((offlineWorker) => {
    const bopts = browserifyOptions(cacheName('push-worker'), 'push-worker.js');
    bopts.debug = false; // no sourcemaps, uglify removes them and they're broken here anyway.
    const b = createBrowserify(bopts);
    return b.bundle()
      .on('error', (e) => {
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
function generateAppcache() {
  if (OFFLINE_WORKERS) {
    const cacheableAssets = gulp.src([
      `${paths.assetsOut}/**/*`,
      `!${paths.assetsOut}/**/*.map`, // don't cache source maps
      `!${paths.assetsOut}/**/*-worker.js`, // don't cache service workers
      `!${paths.assetsOut}/**/swagger.json`,
    ], {
      base: paths.assetsOut,
    });

    return getFontAwesomeVersion().then(faVersion =>
      cacheableAssets
        .pipe(rename(p => {
          if (p.basename === 'fontawesome-webfont') {
            p.extname += `?v=${faVersion}`;
          }
          return p;
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
        .pipe(gulp.dest(paths.assetsOut))
    );
  } else {
    // Produce an empty manifest file
    return gulp.src([])
      .pipe(manifest({
        filename: 'appcache.manifest',
      }))
      .pipe(gulp.dest(paths.assetsOut));
  }
}

gulp.task('service-worker', ['all-static'], generateServiceWorker);
// Run once the scripts and styles are in place
gulp.task('appcache', ['all-static'], generateAppcache);

/* The other watchers have been set up to emit some events that we can listen to */

gulp.task('watch-service-worker', ['watch-styles', 'watch-scripts'], () => {
  bundleEvents.on('styles-updated', generateServiceWorker);
  bundleEvents.on('scripts-updated', generateServiceWorker);
});

gulp.task('watch-appcache', ['watch-styles', 'watch-scripts'], () => {
  bundleEvents.on('styles-updated', generateAppcache);
  bundleEvents.on('scripts-updated', generateAppcache);
});

// Get the current FA version for use in the cache manifest
function getFontAwesomeVersion() {
  return new Promise((resolve, reject) => {
    fs.readFile('node_modules/id7/less/font-awesome/variables.less', (e, data) => {
      if (e) {
        reject(e);
      } else {
        const match = data.toString().match(/fa-version:\s+"(.+)";/);
        if (match) {
          resolve(match[1]);
        } else {
          reject('Pattern not found');
        }
      }
    });
  });
}
