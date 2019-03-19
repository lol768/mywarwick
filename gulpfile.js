'use strict';
/*
 * Build file for JS and CSS assets.
 *
 *     npm install
 *     node_modules/.bin/gulp watch-assets
 */
const gulp = require('gulp');
const fs = require('fs');
const gutil = require('gulp-util');
const eslint = require('gulp-eslint');

/* Recommended gulpopts.json:

 {
  "env": {
   "PRODUCTION": false
  }
 }

 */
let gulpOpts = { env: {} };
try {
  fs.accessSync('./gulpopts.json', fs.R_OK);
  gulpOpts = require('./gulpopts.json');
  gutil.log('Got opts');
} catch (e) {
  gutil.log(gutil.colors.yellow('No gulpopts.json ('+e.message+')'));
}
function option(name, fallback) {
  const value = process.env[name] || gulpOpts.env[name];
  if (value === undefined) return fallback;
  return (value === 'true' || value === true);
}

// Some naughty globals

global.paths = {
  assetPath: 'app/assets',
  scriptOut: 'target/gulp/js',
  assetsOut: 'target/gulp',
  styleIn: [
    'app/assets/css/main.less',
    'app/assets/css/admin.less',
  ],
  styleOut: 'target/gulp/css',
  // Paths under node_modules that will be searched when @import-ing in your LESS.
  styleModules: [
    'id7/less',
  ],
};

global.PRODUCTION = option('PRODUCTION', true);
global.UGLIFY = option('UGLIFY', PRODUCTION);
global.OFFLINE_WORKERS = option('OFFLINE_WORKERS', true);
if (PRODUCTION) {
  gutil.log(gutil.colors.yellow('Production build'));
} else {
  gutil.log(gutil.colors.yellow('Development build'));
}
gutil.log("Uglify: " + UGLIFY);
gutil.log("Appcache/SW generation: " + OFFLINE_WORKERS);

require('./gulp/scripts');
require('./gulp/styles');

gulp.task('all-static', ['id7-static','fontawesome-pro','scripts','styles']);

// Shortcuts for building all asset types at once
gulp.task('assets', ['lint', 'all-static', 'appcache', 'service-worker', 'swagger']);
gulp.task('watch-assets', ['watch-scripts', 'watch-styles']);
gulp.task('wizard', ['appcache','service-worker','watch-assets','watch-service-worker', 'watch-appcache']);

gulp.task('yer-a-wizard', () => {
  gutil.log(gutil.colors.yellow('* * ' + gutil.colors.green('Yer a lizard, Gary!') + '* * '));
});
gulp.task('default', ['yer-a-wizard', 'wizard']);

gulp.task('lint-fix', () => {
  return gulp.src(global.paths.assetPath + '/js/**/*.js')
    .pipe(eslint({ fix: true }))
    .pipe(eslint.format())
    .pipe(gulp.dest(global.paths.assetPath + '/js/'));
});
