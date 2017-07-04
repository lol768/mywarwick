'use strict';

const gulp = require('gulp');
const fs = require('fs');
const gutil = require('gulp-util');
const sourcemaps = require('gulp-sourcemaps');
const replace = require('gulp-replace');
const source = require('vinyl-source-stream');
const buffer = require('vinyl-buffer');
const path = require('path');
const mold = require('mold-source-map');
const playAssets = require('gulp-play-assets');

const babelify = require('babelify');
const webpackStream = require('webpack-stream');
const webpack = require('webpack');
const webpackMerge = require('webpack-merge');
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

const PlayFingerprintsPlugin = require('./PlayFingerprintsPlugin');

const WatchEventsPlugin = function(options) {
  this.apply = this.apply.bind(this, options);
};
WatchEventsPlugin.prototype.apply = (options, compiler) => {
  const emitter = options.emitter;
  compiler.plugin('after-emit', (compilation, done) => {
    emitter.emit('scripts-updated');
    done();
  })
};

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
      gutil.log(gutil.colors.red(e.stack || e));
    })
    .pipe(mold.transformSourcesRelativeTo(path.join(__dirname, '..')))
    .pipe(source(outputFile))
    .pipe(buffer())
    .pipe(sourcemaps.init({ loadMaps: true }))
      .pipe(UGLIFY ? uglify() : gutil.noop())
    .pipe(playAssets())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.scriptOut));
}

// The base non-Gulp Webpack config is in the standard file
const BASE_WEBPACK_CONFIG = require(path.join(__dirname, '../webpack.config.js'));
// Then we merge some extra plugins.
const WEBPACK_CONFIG = webpackMerge(BASE_WEBPACK_CONFIG, {
  plugins: [ new PlayFingerprintsPlugin() ]
});
const WEBPACK_WATCH_CONFIG = webpackMerge(WEBPACK_CONFIG, {
  watch: true,
  plugins: [ new WatchEventsPlugin({ emitter: bundleEvents }) ]
});

function currentRevisionOf(file) {
  const dirname = path.dirname(file);
  const basename = path.basename(file);
  const md5 = fs.readFileSync(`${paths.assetsOut}/${file}.md5`);

  return `${dirname}/${md5}-${basename}`;
}

function getCachedAssets() {
  return [
    currentRevisionOf('/css/main.css'),
    currentRevisionOf('/js/bundle.js'),
    currentRevisionOf('/js/vendor.bundle.js'),
    currentRevisionOf('/js/0.js'),
    currentRevisionOf('/js/3.js'),
    '/lib/id7/fonts/fontawesome-webfont.ttf',
    '/lib/id7/fonts/fontawesome-webfont.woff',
    '/lib/id7/images/masthead-logo-bleed-sm*',
    '/lib/id7/images/masthead-logo-bleed-xs*',
    '/lib/id7/images/newwindow.gif',
    '/lib/id7/images/shim.gif',
    '/lib/id7/js/id7-bundle.min.js',
  ].map((asset) => `${paths.assetsOut}${asset}`);
}

function cacheName(name) {
  return name.replace('.js', '');
}

gulp.task('scripts', () => {
  return webpackStream(WEBPACK_CONFIG, webpack)
    .pipe(gulp.dest('target/gulp/js/'));
});

gulp.task('watch-scripts', [], () => {
  return webpackStream(WEBPACK_WATCH_CONFIG, webpack)
    .pipe(gulp.dest('target/gulp/js/'));
});

gulp.task('lint', () => {
  return gulp.src([`${paths.assetPath}/js/**/*.js`])
    .pipe(eslint('.eslintrc.json'))
    .pipe(eslint.format())
    .pipe(eslint.failAfterError());
});

function generateServiceWorker(watch) {
  const swPrecache = require('sw-precache');
  // Things that should cause fresh HTML to be downloaded
  const htmlDependencies = [
    'target/gulp/css/main.css.md5',
    'target/gulp/js/bundle.js.md5',
    'target/gulp/js/vendor.bundle.js.md5',
    'target/gulp/js/0.js.md5',
    'target/gulp/js/3.js.md5',
    'app/assets/js/push-worker.js',
    'app/views/index.scala.html',
    'app/views/common/head.scala.html',
    'app/views/common/htmlelement.scala.html',
    'app/views/common/id7layout.scala.html',
  ];

  return swPrecache.generate({
    cacheId: 'start',
    handleFetch: OFFLINE_WORKERS,
    staticFileGlobs: [
      'public/**/*',
    ].concat(getCachedAssets()),
    stripPrefixMulti: {
      'target/gulp/' : 'assets/',
      'public/': 'assets/'
    },
    ignoreUrlParametersMatching: [/^v$/],
    logger: gutil.log,
    dynamicUrlToDependencies: {
      '/': htmlDependencies
    },
    // If any of these other URLs are hit, use the same cache entry as /
    // because the HTML is the same for all of them.
    navigateFallback: '/',
    navigateFallbackWhitelist: [
      /^\/notifications/,
      /^\/activities/,
      /^\/search/,
      /^\/news\/?$/,
      /^\/settings/,
    ],
    maximumFileSizeToCacheInBytes: 10 * 1000 * 1000,
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
    const cacheableAssets = merge(
      gulp.src(getCachedAssets(), { base: paths.assetsOut, }),
      gulp.src(['public/**/*'], { base: 'public' })
    );

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
          ],
          hash: true,
          exclude: 'appcache.manifest',
          prefix: '/assets/',
          filename: 'appcache.manifest'
        }))
        .pipe(insert.append('\n# extra cache buster: 1\n'))
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

gulp.task('watch-service-worker', ['watch-styles'], () => {
  bundleEvents.on('styles-updated', generateServiceWorker);
  bundleEvents.on('scripts-updated', generateServiceWorker);
});

gulp.task('watch-appcache', ['watch-styles'], () => {
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
