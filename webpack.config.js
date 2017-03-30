/**
 * Webpack config file. This is now responsible for building the JS assets.
 * It can potentially build other kinds of assets too, but let's not get crazy.
 *
 */

const path = require('path');
const fs = require('fs');
const webpack = require('webpack');

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
      compress: {
        unsafe: true,
        screw_ie8: true,
      }
    }));
  }
  return plugins;
}

module.exports = {
  entry: {
    bundle: './app/assets/js/main.js',
    'publish-bundle': './app/assets/js/publish.js',
  },
  output: {
    path: path.resolve(__dirname, 'target/gulp/js'),
    filename: '[name].js',
    chunkFilename: '[chunkhash]-[id].js',
    publicPath: '/assets/js/',
  },
  resolve: {
    alias: {
      // Force some duplicate modules to come from one place
      'whatwg-fetch' : topLevelModule('whatwg-fetch'),
      'reselect' : topLevelModule('reselect'),
      'draggable-core' : topLevelModule('draggable-core'),
      'react-draggable' : topLevelModule('react-draggable'),
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
        test: /\.jsx?$/,
        // Only run Babel on these modules:
        include: [
          path.resolve(__dirname, 'app/assets/js'),
          path.resolve(__dirname, 'node_modules/warwick-search-frontend/app/assets/js'),
        ],
        loader: 'babel-loader',
        options: Object.assign({},
          readJSON('.babelrc'),
          { cacheDirectory: 'target/babel-loader-cache' }
        ),
      },
    ],
  },
  devtool: 'source-map',
  plugins: getPlugins(),
};
