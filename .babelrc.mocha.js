module.exports = {
  /**
   * We build a few modules ourselves, and unfortunately have to
   * tell it which ones to build here or it won't Babel 'em.
   */
  only: [
    /assets\/js/,
    /lodash-es/,
    /warwick-search/,
    /es6-promise/,
  ],
  plugins: [
    "@babel/plugin-transform-modules-commonjs",
    ["dynamic-import-node", { "noInterop": true }] // Webpack does our dynamic imports normally,
                          // this shims import() so it works in Node.
  ]
}