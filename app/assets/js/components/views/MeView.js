const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

import MailTile from '../tiles/mail';

export default class MeView extends ReactComponent {

    render() {
        return (
            <div>
                <MailTile size="large" />
            </div>
        );
    }

}
