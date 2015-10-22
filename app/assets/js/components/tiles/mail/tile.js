import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import Tile from '../Tile';

export default class MailTile extends ReactComponent {

    render() {
        switch (this.props.size) {
            case 'large':
                return (
                    <Tile title="Mail" href="http://warwick.ac.uk/mymail" backgroundColor="#0078d7" icon="envelope-o">
                        <ul className="list-unstyled">
                            {this.props.messages.map((message) => <MailMessageListItem key={message.id} {...message} />)}
                        </ul>
                    </Tile>
                );
            default:
                return (
                    <Tile title="Mail" href="http://warwick.ac.uk/mymail" backgroundColor="#0078d7" icon="envelope-o">
                        {this.props.messages.size()} unread
                    </Tile>
                );
        }
    }

}

class MailMessageListItem extends ReactComponent {

    render() {

        return (
            <li className="mail-list-item">
                <span className="mail-list-item__from">{this.props.from}</span>
                <span className="mail-list-item__date">{this.props.date}</span>
                <span className="mail-list-item__subject">{this.props.subject}</span>
            </li>
        );

    }

}
