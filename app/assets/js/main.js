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

const NotificationActions = require('./NotificationActions');
const NotificationsStore = require('./stores/NotificationsStore');

(()=> {

    localforage.config({
        name: 'Start'
    });

    // String replaced by Gulp build.
    const BUILD_TIME = "$$BUILDTIME$$";

    log.info("Scripts built at:", BUILD_TIME);

})();

$(function () {

    var socket = new WebSocket('wss://finn.warwick.ac.uk/websockets/page');

    socket.onopen = function() {
        socket.send(JSON.stringify({
            messageId: 1,
            tileId: "some-tile",
            data: {
                type: 'get-current-notifications'
            }
        }));
    };

    socket.onmessage = function(event){
        //console.log(event.data);
        NotificationActions.didReceiveNotification(JSON.parse(event.data));
    }

    ReactDOM.render(<UtilityBar name="John Smith"/>, document.getElementById('utility-bar-container'));
    ReactDOM.render(<Application />, document.getElementById('app-container'));

    window.addEventListener('popstate', function() {
        AppActions.navigate(window.location.pathname);
    });

});
