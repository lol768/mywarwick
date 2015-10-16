global.expect = require('chai').expect;
global.sinon = require('sinon');

global.document = require('jsdom').jsdom();
global.window = global.document.defaultView;
global.navigator = global.window.navigator;

global.React = require('react');
global.ReactTestUtils = require('react-addons-test-utils');
