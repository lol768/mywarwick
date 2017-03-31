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
 * (and for external modules it will find the closest .babelrc,
 * so they will end up using their own settings).
 */

require('babel-register')({
  /**
   * We build a few modules ourselves, and unfortunately have to
   * tell it which ones to build here or it won't Babel 'em.
   */
  only: [
    /assets\/js/,
    /lodash-es/,
    /warwick-search/
  ],
  plugins: [
    'dynamic-import-node' // Webpack does our dynamic imports normally,
                          // this shims import() so it works in Node.
  ]
});
