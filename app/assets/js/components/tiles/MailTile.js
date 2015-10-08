const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const Tile = require('./Tile');

export default class MailTile extends ReactComponent {

    constructor(props) {
        super(props);

        this.state = {
            unreadMessages: [
                {
                    from: 'IT Service Desk',
                    subject: 'Emergency RFC0021650 - Network',
                    date: '09:48'
                },
                {
                    from: 'Kennedy, Christelle',
                    subject: 'Departmental meeting',
                    date: 'Yesterday'
                },
                {
                    from: 'Squirrell, Linda',
                    subject: 'IT Induction Day - Reminder',
                    date: 'Monday'
                }
            ]
        };
    }

    render() {
        switch (this.props.size) {
            case 'large':
                return (
                    <Tile title="Mail" href="http://warwick.ac.uk/mymail">
                        <ul className="list-unstyled">
                            {this.state.unreadMessages.map((message) => <MailMessageListItem {...message} />)}
                        </ul>
                    </Tile>
                );
            default:
                return (
                    <Tile title="Mail" href="http://warwick.ac.uk/mymail">
                        {this.state.unreadMessages.length} unread
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