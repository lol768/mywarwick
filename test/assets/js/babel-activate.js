/**
 * There's a Mocha option to enable babel compilation:
 *
 *    --compilers js:babel-register
 *
 * but it doesn't let you pass options to babel. This does
 * the same thing but lets us pass options. Simply require
 * in Mocha before anything else:
 *
 *    -r babel-activate
 *
 * These options are ADDED to what's already in .babelrc
 */

require('babel-register')({
  plugins: [
    'dynamic-import-node' // Webpack does our dynamic imports normally,
                          // this shims import() so it works in Node.
  ]
});
