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
const _ = require('lodash');

const bundleEvents = require('./events');

const PlayFingerprintsPlugin = require('./PlayFingerprintsPlugin');

const WatchEventsPlugin = function (options) {
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
  plugins: [new PlayFingerprintsPlugin()]
});
const WEBPACK_WATCH_CONFIG = webpackMerge(WEBPACK_CONFIG, {
  watch: true,
  plugins: [new WatchEventsPlugin({ emitter: bundleEvents })]
});

function currentRevisionOfAsync(file, i) {
  if (i === undefined) {
    i = 0;
  }

  const dirname = path.dirname(file);
  const basename = path.basename(file);
  const md5Path = `${paths.assetsOut}/${file}.md5`;

  if (i >= 20) {
    throw new Error(`File ${md5Path} could not be read`);
  }

  return new Promise((resolve) => {
    fs.readFile(md5Path, (err, md5) => {
      if (err) {
        setTimeout(() => resolve(currentRevisionOfAsync(file, i + 1)), 500);
      } else {
        resolve(`${dirname}/${md5}-${basename}`);
      }
    });
  });
}

function getCachedAssetsAsync() {
  return Promise.all([
    currentRevisionOfAsync('/css/main.css'),
    currentRevisionOfAsync('/js/bundle.js'),
    currentRevisionOfAsync('/js/vendor.bundle.js'),
    currentRevisionOfAsync('/js/0.js'),
    // following NEWSTART-1074 change, webpack started writing to 4.js instead of 3.js, which appeared after NEWSTART-1307, instead of 1.js.
    // so much shrug
    currentRevisionOfAsync('/js/4.js'),
    Promise.resolve('/lib/id7/fonts/fontawesome-webfont.ttf'),
    Promise.resolve('/lib/id7/fonts/fontawesome-webfont.woff'),
    Promise.resolve('/lib/id7/images/masthead-logo-bleed-sm*'),
    Promise.resolve('/lib/id7/images/masthead-logo-bleed-xs*'),
    Promise.resolve('/lib/id7/images/newwindow.gif'),
    Promise.resolve('/lib/id7/images/shim.gif'),
    Promise.resolve('/lib/id7/js/id7-bundle.min.js'),
  ]).then(array => array.map(asset => path.join(paths.assetsOut, asset)));
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
  const generateSW = require('workbox-build').generateSW;

  // Things that should cause fresh HTML to be downloaded
  const htmlDependencies = [
    'target/gulp/css/main.css.md5',
    'target/gulp/js/bundle.js.md5',
    'target/gulp/js/vendor.bundle.js.md5',
    'target/gulp/js/0.js.md5',
    // following NEWSTART-1307 change webpack started writing to 3.js instead of 1.js shrug
    'target/gulp/js/3.js.md5',
    'app/assets/js/push-worker.js',
    'app/views/index.scala.html',
    'app/views/common/head.scala.html',
    'app/views/common/htmlelement.scala.html',
    'app/views/common/id7layout.scala.html',
  ];

  // Scripts that should be imported from the Service Worker
  const commonSWScripts = [
    'assets/js/push-worker.js',
  ];

  const debugSWScripts = [
    'assets/js/sw-debug.js',
  ];

  const swScripts = PRODUCTION ? commonSWScripts : [].concat(commonSWScripts).concat(debugSWScripts);

  const swConfig = {
    swDest: path.join(paths.assetsOut, 'service-worker.js'),
    importWorkboxFrom: 'cdn',
    cacheId: 'start',
    globDirectory: path.join(__dirname, '..'),
    modifyUrlPrefix: {
      'target/gulp/': 'assets/',
      'public/': 'assets/'
    },
    dontCacheBustUrlsMatching: /\/[0-9a-f]{32}-/,
    ignoreUrlParametersMatching: [/^v$/],
    templatedUrls: {
      '/': htmlDependencies
    },
    // If any of these other URLs are hit, use the same cache entry as /
    // because the HTML is the same for all of them.
    navigateFallback: '/',
    navigateFallbackWhitelist: [
      /^\/notifications/,
      /^\/alerts/,
      /^\/activities/,
      /^\/search/,
      /^\/news\/?$/,
      /^\/settings/,
    ],
    maximumFileSizeToCacheInBytes: 10 * 1000 * 1000,
    importScripts: swScripts,
    skipWaiting: true,
    clientsClaim: true,
  };

  return getCachedAssetsAsync()
    .then(cachedAssets => {
      const assetCaching = {
        globPatterns: [
          'public/**/*',
        ].concat(cachedAssets),
      };

      const workboxConfig = OFFLINE_WORKERS ? Object.assign({}, swConfig, assetCaching) : swConfig;

      return generateSW(workboxConfig);
    })
    .then((results) => {
      gutil.log(`Generated service worker with ${results.count} pre-cached entries totalling ${results.size}b`);
      results.warnings.forEach(w => gutil.log(gutil.colors.yellow(w.toString())));
    });
}

// Not strictly a script thing, but here it is in scripts.js.
function generateAppcache() {
  if (OFFLINE_WORKERS) {
    return Promise.all([
      getFontAwesomeVersion(),
      getCachedAssetsAsync(),
    ]).then((results) => {
      const faVersion = results[0];
      const cachedAssets = results[1];

      const cacheableAssets = merge(
        gulp.src(cachedAssets, { base: paths.assetsOut, }),
        gulp.src(['public/**/*'], { base: 'public' })
      );

      return cacheableAssets
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
            '/alerts',
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
        .pipe(gulp.dest(paths.assetsOut));
    });
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
  bundleEvents.once('scripts-updated', (watch) => {
    generateServiceWorker(watch);
    bundleEvents.on('styles-updated', generateServiceWorker);
    bundleEvents.on('scripts-updated', generateServiceWorker);
  });
});

gulp.task('watch-appcache', ['watch-styles'], () => {
  bundleEvents.once('scripts-updated', (watch) => {
    generateAppcache(watch);
    bundleEvents.on('styles-updated', generateAppcache);
    bundleEvents.on('scripts-updated', generateAppcache);
  });
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
