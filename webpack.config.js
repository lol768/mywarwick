/**
 * Webpack config file. This is now responsible for building the JS assets.
 * It can potentially build other kinds of assets too, but let's not get crazy.
 *
 */

const path = require('path');
const fs = require('fs');
const webpack = require('webpack');
const webpackMerge = require('webpack-merge');

if (global.PRODUCTION === undefined) {
  global.PRODUCTION = (process.env.PRODUCTION === 'true');
  console.warn(`Var PRODUCTION is undefined. Usually the Gulpfile would set this. Falling back to process.env.PRODUCTION (${PRODUCTION})`);
}

function readJSON (filepath) {
  return JSON.parse(fs.readFileSync(path.join(__dirname, filepath)));
}

function topLevelModule (name) {
  return path.join(__dirname, 'node_modules', name);
}

// Generate list of plugins to use, with some being conditional.
function getPlugins() {
  const plugins = [
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify(PRODUCTION ? 'production' : 'development')
    }),
    // stop moment pulling in ALL locales. Just en is fine
    new webpack.ContextReplacementPlugin(/moment[\\\/]locale$/, /^\.\/(en)$/),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor', filename: 'vendor.bundle.js'
    }),
  ];

  if (PRODUCTION) {
    plugins.push(new webpack.optimize.UglifyJsPlugin({
      sourceMap: true,
      comments: false,
      minify: true,
      compress: {
        unsafe: true,
        screw_ie8: true,
        warnings: false,
      }
    }));
  }
  return plugins;
}

module.exports = {
  entry: {
    bundle: './app/assets/js/boot.js',
    'admin-bundle': './app/assets/js/admin.js',
    'publish-bundle': './app/assets/js/publish.js',
    vendor: ['react','react-dom','redux','react-redux','moment'],
  },
  output: {
    path: path.resolve(__dirname, 'target/gulp/js'),
    filename: '[chunkhash]-[name].js',
    chunkFilename: '[chunkhash]-[name].js',
    publicPath: '/assets/js/',
  },
  resolve: {
    extensions: ['.js', '.jsx', '.es6'],
    alias: {
      // Force some duplicate modules to come from one place
      'whatwg-fetch' : topLevelModule('whatwg-fetch'),
      'reselect' : topLevelModule('reselect'),
      'draggable-core' : topLevelModule('draggable-core'),
      'react-draggable' : topLevelModule('react-draggable/index'),
      // Force some modules to use the ES6 source, for better optimisation.
      // May also need to add to babel-activate.js in the tests.
      'lodash' : 'lodash-es',
      'localforage' : 'localforage/src/localforage',
      // Replace some Search stuff we don't need with a big fake module
      './CuratedLinkForm' : path.resolve(__dirname, 'app/assets/js/components/FakeSearchModule.js'),
    }
  },
  externals: {
    jquery: '$',
    modernizr: 'Modernizr',
  },
  module: {
    rules: [
      {
        test: /\.(es6|jsx?)$/,
        // Only run Babel on these modules:
        include: [
          path.resolve(__dirname, 'app/assets/js'),
          // Some 3rd party modules that need compiling too
          topLevelModule('localforage/src'),
          topLevelModule('react-draggable/lib'),
        ],
        loader: 'babel-loader',
        options: webpackMerge(
          readJSON('.babelrc'),
          {
            presets: [
              // modules:false keeps `import` statements as they are,
              // and lets webpack take advantage of extra optimisations.
              ['es2015', { modules: false }]
            ],
            cacheDirectory: 'target/babel-loader-cache'
          }
        ),
      },
      // Search in a separate rule so we can override babelrc to disable
      // a pesky babel-rewire plugin.
      {
        test: /\.(es6|jsx?)$/,
        // Only run Babel on these modules:
        include: [
          topLevelModule('warwick-search-frontend/app/assets/js')
        ],
        use: {
          loader: 'babel-loader',
          query: {
            babelrc: false,
            plugins: [],
            presets: [
              'airbnb', 'react'
            ],
            cacheDirectory: 'target/babel-loader-search-cache'
          }
        }
      },
    ],
  },
  devtool: 'source-map',
  plugins: getPlugins(),
};
