const log = require('loglevel');
// only 'warn' otherwise
log.enableAll(false);

const $ = require('jquery');

const localforage = require('localforage');

const React = require('react');
const ReactDOM = require('react-dom');

const Application = require('./components/Application');
const UtilityBar = require('./components/ui/UtilityBar');

import store from './store';
window.Store = store;
import { navigate } from './actions';

import { Provider } from 'react-redux';

import './update';

require('./notifications');

(()=> {

    localforage.config({
        name: 'Start'
    });

    // String replaced by Gulp build.
    const BUILD_TIME = "$$BUILDTIME$$";

    log.info("Scripts built at:", BUILD_TIME);

})();

var currentPath = '/';

$(function () {

    ReactDOM.render(<UtilityBar name="John Smith"/>, document.getElementById('utility-bar-container'));
    ReactDOM.render(
        <Provider store={store}>
            <Application />
        </Provider>,
        document.getElementById('app-container'));

    window.addEventListener('popstate', function () {
        currentPath = window.location.pathname;
        store.dispatch(navigate(window.location.pathname));
    });

});

store.subscribe(() => {
    console.log('Store updated', store.getState().toJS());
});

store.subscribe(() => {
    var path = store.getState().get('path');

    if (path != currentPath) {
        currentPath = path;

        if (window.history.pushState) {
            window.history.pushState(null, null, currentPath);
        }
    }
});
