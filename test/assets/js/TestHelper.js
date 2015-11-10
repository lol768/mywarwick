global.expect = require('chai').expect;
global.assert = require('chai').assert;
global.sinon = require('sinon');

global.document = require('jsdom').jsdom();
global.window = global.document.defaultView;
global.navigator = global.window.navigator;

global.React = require('react');
global.ReactTestUtils = require('react-addons-test-utils');

// Do helpful things with Spies.  Use inside a test suite (`describe' block).
global.spy = function spy(object, method) {

  // Spy on the method before any tests run
  before(() => {
    sinon.spy(object, method);
  });

  // Re-initialise the spy before each test
  beforeEach(() => {
    object[method].reset();
  });

  // Restore the original method after all tests have run
  after(() => {
    object[method].restore();
  });

};

global.shallowRender = function shallowRender(component) {
  let renderer = ReactTestUtils.createRenderer();
  renderer.render(component);

  return renderer.getRenderOutput();
};