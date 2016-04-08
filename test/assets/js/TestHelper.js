import tk from 'timekeeper';

const chai = require('chai');
global.expect = chai.expect;
global.should = chai.should();
global.assert = chai.assert;
global.sinon = require('sinon');
chai.use(require('sinon-chai'));

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

global.renderAtMoment = function (component, now = new Date(1989, 1, 7)) {
  tk.freeze(new Date(now));
  const renderedComponent = shallowRender(component);
  tk.reset();
  return renderedComponent;
};
