describe('dynamicImport', () => {

  /**
   * All this really tests at the moment is that the
   * dynamic-import-node plugin is set up properly in
   * mocha-babel-activate.js, to make dynamic imports not
   * explode in Node. Webpack handles it differently.
   */
  it('works in Node', () => {
    return import('path').then((path) => {
      path.join('/foo','bar','..','lol').should.equal('/foo/lol');
    });
  });

});
