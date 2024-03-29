'use strict';

const gulp = require('gulp');
const path = require('path');
const playAssets = require('gulp-play-assets');

const sourcemaps = require('gulp-sourcemaps');
const less = require('gulp-less');

const postcss = require('gulp-postcss');
const autoprefix = require('autoprefixer');
const swagger = require('gulp-swagger');

const bundleEvents = require('./events');

/**
 * Copies static resources out of an NPM module, and into
 * the asset output directory.
 */
function exportAssetModule(name, taskName, baseDir, extraExtensions) {
  gulp.task(taskName, () => {
    const base = 'node_modules/' + name + '/' + baseDir;

    const baseExtensions = ['otf', 'eot', 'woff', 'woff2', 'ttf', 'js', 'js.map', 'gif', 'png', 'jpg', 'svg', 'ico'];
    const srcs = (extraExtensions || []).concat(baseExtensions);
    const srcPaths = srcs.map( s => `${base}/**/*.${s}` );

    return gulp.src(srcPaths, { base: base })
      .pipe(gulp.dest(paths.assetsOut + '/lib/' + name))
  });
}

exportAssetModule('id7', 'id7-static', 'dist');

gulp.task('styles', cb => {
  const styleJob = gulp.src(paths.styleIn)
    .pipe(sourcemaps.init())
    .pipe(less({
      // Allow requiring less relative to node_modules, plus any other dir under node_modules
      // that's in styleModules.
      paths: [path.join(__dirname, '..', 'node_modules')]
        .concat(paths.styleModules.map(modulePath =>
          path.join(__dirname, '..', 'node_modules', modulePath)
        )),
    }))
    .on('error', err => cb(err.message))
    .pipe(postcss([
      autoprefix({ browsers: 'last 1 version' }),
    ]))
    .pipe(playAssets())
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.styleOut));

  styleJob.on('finish', () => {
    bundleEvents.emit('styles-updated');
  });

  return styleJob;
});

// Recompile LESS on changes
gulp.task('watch-styles', ['styles'], () => {
  return gulp.watch(paths.assetPath + '/css/**/*.less', ['styles']);
});

gulp.task('swagger', () => {
  // public API spec
  gulp.src([paths.assetPath + '/swagger/swagger_public.yml'])
    .pipe(swagger('swagger_public.json'))
    .pipe(gulp.dest(paths.assetsOut));
  // internal API spec
  gulp.src([paths.assetPath + '/swagger/swagger.yml'])
    .pipe(swagger('swagger.json'))
    .pipe(gulp.dest(paths.assetsOut));
});
