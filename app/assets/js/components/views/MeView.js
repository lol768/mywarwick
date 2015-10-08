const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const MailTile = require('../tiles/MailTile');

export default class MeView extends ReactComponent {

    render() {
        return (
            <div>
                <MailTile size="large" />
            </div>
        );
    }

}
