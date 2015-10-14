const log = require('loglevel');
// only 'warn' otherwise
log.enableAll(false);

const $ = require('jquery');

const localforage = require('localforage');

const React = require('react');
const ReactDOM = require('react-dom');

const Application = require('./components/Application');
const UtilityBar = require('./components/ui/UtilityBar');

const AppActions = require('./AppActions');

const Dispatcher = require('./Dispatcher');

(()=> {

    localforage.config({
        name: 'Start'
    });

    // String replaced by Gulp build.
    const BUILD_TIME = "$$BUILDTIME$$";

    log.info("Scripts built at:", BUILD_TIME);

})();

$(function () {

    ReactDOM.render(<UtilityBar name="John Smith"/>, document.getElementById('utility-bar-container'));
    ReactDOM.render(<Application />, document.getElementById('app-container'));

    window.addEventListener('popstate', function() {
        AppActions.navigate(window.location.pathname);
    });

    if (window.applicationCache) {
        function onDownloading() {
            Dispatcher.dispatch({
                type: 'app-update-start'
            });
        }

        function onProgress(e) {
            Dispatcher.dispatch({
                type: 'app-update-progress',
                loaded: e.loaded,
                total: e.total
            });
        }

        function onUpdateReady() {
            Dispatcher.dispatch({
                type: 'app-update-ready'
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
