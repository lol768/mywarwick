/*
 * Build file for JS and CSS assets.
 *
 *     npm install
 *     node_modules/.bin/gulp watch-assets
 */
'use strict';

var gulp       = require('gulp');
var gutil = require('gulp-util');
var less       = require('gulp-less');
var sourcemaps = require('gulp-sourcemaps');
var uglify     = require('gulp-uglify');
var browserify = require('browserify');
var babelify   = require('babelify');
var watchify   = require('watchify');
var exorcist   = require('exorcist');
var source     = require('vinyl-source-stream');
var buffer     = require('vinyl-buffer');
var _          = require('lodash');

var paths = {
  assetPath: 'app/assets',

  scriptIn: ['app/assets/js/main.js'],
  scriptOut: 'target/web/public/main/js',

  styleIn: ['app/assets/css/main.less', 'node_modules/id7/less/id7.less'],
  styleOut: 'target/web/public/main/css',
};

var browserifyOptions = {
  entries: paths.scriptIn,
  debug: true, // confusingly, this enables sourcemaps
  transform: [ babelify ] // Transforms ES6 + JSX into normal JS
};

var uglifyOptions = {/*defaults*/};

// Function for running Browserify on JS, since
// we reuse it a couple of times.
var bundle = function(browserify) {
  browserify.bundle()
    .on('error', gutil.log.bind(gutil, 'Browserify Error'))
    .pipe(exorcist(paths.scriptOut + "/bundle.js.map"))
    .pipe(source('bundle.js'))
    .pipe(buffer())
    .pipe(sourcemaps.init({loadMaps: true}))
      .pipe(uglify(uglifyOptions))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.scriptOut))
}

gulp.task('scripts', [], function() {
  var b = browserify(browserifyOptions);
  return bundle(b);
});

// Recompile scripts on changes. Watchify is more efficient than
// grunt.watch as it knows how to do incremental rebuilds.
gulp.task('watch-scripts', [], function() {
  var bw = watchify(browserify(_.assign({}, watchify.args, browserifyOptions)));
  bw.on('update', function() { bundle(bw); });
  bw.on('log', gutil.log);
  return bundle(bw);
});

gulp.task('styles', function() {
  return gulp.src(paths.styleIn)
    .pipe(sourcemaps.init())
    .pipe(less())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.styleOut))
});

// Recompile LESS on changes
gulp.task('watch-styles', function() {
  return gulp.watch(paths.assetPath+'/css/**/*.less', ['styles']);
})

// Shortcuts for building all asset types at once
gulp.task('assets', ['scripts','styles']);
gulp.task('watch-assets', ['watch-scripts','watch-styles']);
