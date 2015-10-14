const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const localforage = require('localforage');

const MailTile = require('../tiles/MailTile');

export default class MeView extends ReactComponent {

    render() {

        // using this render to clear local forage db
        localforage.clear();

        return (
            <div>
                <MailTile size="large" />
            </div>
        );
    }

}
