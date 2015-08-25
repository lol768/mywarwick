/*
 * Build file for JS and CSS assets.
 *
 *     npm install
 *     node_modules/.bin/gulp watch-assets
 */
'use strict';

var gulp       = require('gulp');
var gutil      = require('gulp-util');
var sourcemaps = require('gulp-sourcemaps');
var replace    = require('gulp-replace');
var source     = require('vinyl-source-stream');
var buffer     = require('vinyl-buffer');
var _          = require('lodash');
var path       = require('path');
var mold       = require('mold-source-map');

var postcss    = require('gulp-postcss');
var less       = require('gulp-less');
var uglify     = require('gulp-uglify');
var browserify = require('browserify');
var babelify   = require('babelify');
var watchify   = require('watchify');
var exorcist   = require('exorcist');
var autoprefix = require('autoprefixer-core');

var paths = {
  assetPath: 'app/assets',

  scriptIn: ['app/assets/js/main.js'],
  scriptOut: 'target/gulp/js',

  assetsOut: 'target/gulp',

  styleIn: ['app/assets/css/main.less', 'node_modules/id7/less/id7.less'],
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
  transform: [ babelify ] // Transforms ES6 + JSX into normal JS
};

var uglifyOptions = {/*defaults*/};

console.log(path.join(__dirname, 'app', 'assets'))

// Function for running Browserify on JS, since
// we reuse it a couple of times.
var bundle = function(browserify) {
  browserify.bundle()
    .on('error', function(e) {
      gutil.log(gutil.colors.red(e.toString()));
    })
    .pipe(mold.transformSourcesRelativeTo(path.join(__dirname, 'app', 'assets', 'js')))
    //.pipe(exorcist(paths.scriptOut + "/bundle.js.map"))
    .pipe(source('bundle.js'))
    .pipe(buffer())
      .pipe(sourcemaps.init({loadMaps: true}))
      .pipe(replace('$$BUILDTIME$$', (new Date()).toString()))
      .pipe(uglify())
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

/**
 * Copies static resources out of an NPM module, and into
 * the asset output directory.
 */
function exportAssetModule(name, taskName, baseDir, extraExtensions) {
  gulp.task(taskName, function() {
    var base = 'node_modules/' + name + '/' + baseDir;

    var baseExtensions = ['woff','woff2','ttf','js','js.map','gif','png','jpg','svg'];
    var srcs = (extraExtensions || []).concat(baseExtensions);
    var srcPaths = srcs.map(function(s) { return base + '/**/*.' + s; });

    return gulp.src(srcPaths, {base:base})
      .pipe(gulp.dest(paths.assetsOut + '/lib/' + name))
  });
}

exportAssetModule('leaflet', 'leaflet-static', 'dist', ['css']);
exportAssetModule('id7', 'id7-static', 'dist');
//exportAssetModule('material-design-lite', 'material-static', '');

gulp.task('styles', ['id7-static','leaflet-static'], function() {
  return gulp.src(paths.styleIn)
    .pipe(sourcemaps.init())
    .pipe(less({
      // Allow requiring less relative to node_modules, plus any other dir under node_modules
      // that's in styleModules.
      paths: [path.join(__dirname, 'node_modules')].concat(paths.styleModules.map(function(modulePath) {
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
gulp.task('watch-styles', ['styles'], function() {
  return gulp.watch(paths.assetPath+'/css/**/*.less', ['styles']);
})

// Shortcuts for building all asset types at once
gulp.task('assets', ['scripts','styles']);
gulp.task('watch-assets', ['watch-scripts','watch-styles']);
gulp.task('wizard', ['watch-assets']);
