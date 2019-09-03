import tk from 'timekeeper';
import jsxChai from 'jsx-chai';
import * as enzyme from 'enzyme';
import ShallowRenderer from 'react-test-renderer/shallow';

const chai = require('chai');
global.expect = chai.expect;
global.should = chai.should();
global.assert = chai.assert;
global.sinon = require('sinon');
chai.use(require('sinon-chai'));
chai.use(jsxChai);

const JSDOM = require("jsdom").JSDOM;

function setupBlankDocument() {
  global.window = new JSDOM().window;
  global.document = global.window.document;
  global.navigator = global.window.navigator;
}

setupBlankDocument();

class WebSocket {}
global.WebSocket = WebSocket;

global.React = require('react');
global.ReactTestUtils = require('react-dom/test-utils');

const FROZEN_IN_TIME = new Date(1989, 1, 7);

// Do helpful things with Spies.  Use inside a test suite (`describe' block).
global.spy = function spy(object, method) {

  // Spy on the method before any tests run
  before(function () {
    sinon.spy(object, method);
  });

  // Re-initialise the spy before each test
  beforeEach(function () {
    object[method].reset();
  });

  // Restore the original method after all tests have run
  after(function() {
    //restore method doesn't exist when I tried it.
    object[method].restore();
  });

};

global.shallowRender = function shallowRender(component) {
  let renderer = new ShallowRenderer();
  renderer.render(component);

  return renderer.getRenderOutput();
};


global.atMoment = function(fn, now = FROZEN_IN_TIME) {
  tk.freeze(new Date(now));
  const result = fn();
  tk.reset();
  return result;
};

/** shallow render at a given fake time, using React's standard shallow renderer. */
global.renderAtMoment = function (component, now = FROZEN_IN_TIME) {
  return atMoment(() => shallowRender(component), now);
};

/** shallow render at a given fake time, returning an Enzyme wrapper.
 * {@link http://airbnb.io/enzyme/docs/api/shallow.html#shallowwrapper-api API docs}
 */
global.shallowAtMoment = function(component, now = FROZEN_IN_TIME) {
  return atMoment(() => enzyme.shallow(component), now);
};

global.mountAtMoment = function(component, now = FROZEN_IN_TIME) {
  return atMoment(() => enzyme.mount(component), now);
};

/**
 * Recursively call props.children[i] in accordance with path param
 *
 * @param {object} elem find children of this rendered element
 * @param {number[]} path path of indicies to target child
 * @returns {object}
 *
 * @deprecated This is a bit brittle, try importing 'shallow' from 'enzyme' instead
 */
global.findChild = function (elem, path) {
  if (elem === 'undefined' || path.length === 0) {
    return elem;
  }
  return findChild(React.Children.toArray(elem.props.children)[path[0]], path.slice(1));
};
