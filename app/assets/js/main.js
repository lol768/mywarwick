const log = require('loglevel');
// only 'warn' otherwise
log.enableAll(false);

const $ = window.jQuery;

const localforage = require('localforage');

const React = require('react');
const ReactDOM = require('react-dom');

const Application = require('./components/Application');
const UtilityBar = require('./components/ui/UtilityBar');

(()=> {

    localforage.config({
        name: 'Start'
    })

})();

$(function () {

    ReactDOM.render(<UtilityBar name="John Smith" />, document.getElementById('utility-bar-container'));
    ReactDOM.render(<Application />, document.getElementById('app-container'));

});
