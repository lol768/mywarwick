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

import { registerReducer } from './reducers';

import Immutable from 'immutable';
const initialState = Immutable.fromJS({
    isUpdating: false,
    loaded: 0,
    total: 0
});

registerReducer('update', (state = initialState, action) => {
    console.log('update reducer', state, action);
    switch (action.type) {
        case 'update.start':
            return state.merge({
                isUpdating: true
            });
        case 'update.progress':
            return state.merge({
                isUpdating: true,
                loaded: action.loaded,
                total: action.total
            });
        case 'update.ready':
            return state.merge({
                isUpdating: true,
                loaded: state.get('total')
            });
        default:
            return state;
    }
});

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

    if (window.applicationCache) {
        function onDownloading() {
            store.dispatch({
                type: 'update.start'
            });
        }

        function onProgress(e) {
            store.dispatch({
                type: 'update.progress',
                loaded: e.loaded,
                total: e.total
            });
        }

        function onUpdateReady() {
            store.dispatch({
                type: 'update.ready'
            });
        }

        window.applicationCache.addEventListener('progress', onProgress);
        window.applicationCache.addEventListener('downloading', onDownloading);
        window.applicationCache.addEventListener('updateready', onUpdateReady);

        if (window.applicationCache.status == window.applicationCache.UPDATEREADY) {
            onUpdateReady();
        }
    }

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
