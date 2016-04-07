var gulp = require('gulp');
var gutil = require('gulp-util');
var path = require('path');

var sourcemaps = require('gulp-sourcemaps');
var less = require('gulp-less');

var autoprefix = require('autoprefixer-core');
var filenames = require('gulp-filenames');
var insert = require('gulp-insert');
var manifest = require('gulp-manifest');
var postcss = require('gulp-postcss');
var swagger = require('gulp-swagger');
var watchify = require('watchify');
var merge = require('merge-stream');

var bundleEvents = require('./events');

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

    return gulp.src(srcPaths, { base: base })
      .pipe(gulp.dest(paths.assetsOut + '/lib/' + name))
  });
}

exportAssetModule('id7', 'id7-static', 'dist');

gulp.task('styles', function () {
  var styleJob = gulp.src(paths.styleIn)
    .pipe(sourcemaps.init())
    .pipe(less({
      // Allow requiring less relative to node_modules, plus any other dir under node_modules
      // that's in styleModules.
      paths: [path.join(__dirname, '..', 'node_modules')].concat(paths.styleModules.map(function (modulePath) {
        return path.join(__dirname, '..', 'node_modules', modulePath)
      }))
    }))
    .pipe(postcss([
      autoprefix({ browsers: 'last 1 version' })
    ]))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.styleOut));

  styleJob.on('finish', function () {
    bundleEvents.emit('styles-updated');
  });
  
  return styleJob;
});

// Recompile LESS on changes
gulp.task('watch-styles', ['styles'], function () {
  return gulp.watch(paths.assetPath + '/css/**/*.less', ['styles']);
});

gulp.task('swagger', function() {
  gulp.src([paths.assetPath + '/swagger.yml'])
    .pipe(swagger('swagger.json'))
    .pipe(gulp.dest(paths.assetsOut));
});
