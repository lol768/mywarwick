const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const Tile = require('./Tile');

const Dispatcher = require('../../Dispatcher');
const FluxStore = require('flux/lib/FluxStore');

const Immutable = require('immutable');

var messages = Immutable.List();

class MailStore extends FluxStore {

    getMessages() {
        return messages;
    }

    __onDispatch(action) {
        switch (action.type) {
            case 'mail-receive':
                messages = messages.unshift(action.mail);
                this.__emitChange();
                break;
            default:
            // no-op
        }
    }

}

let Store = new MailStore(Dispatcher);

class MailActions {

    static didRecieveMail(mail) {
        Dispatcher.dispatch({
            type: 'mail-receive',
            mail: mail
        });
    }

}

MailActions.didRecieveMail({
    id: 1,
    from: 'Squirrell, Linda',
    subject: 'IT Induction Day - Reminder',
    date: 'Monday'
});

setTimeout(() => {
    MailActions.didRecieveMail({
        id: 2,
        from: 'Kennedy, Christelle',
        subject: 'Departmental meeting',
        date: 'Yesterday'
    });
}, 1000);

setTimeout(() => {
    MailActions.didRecieveMail({
        id: 3,
        from: 'IT Service Desk',
        subject: 'Emergency RFC0021650 - Network',
        date: '09:48'
    });
}, 2000);

export default class MailTile extends ReactComponent {

    constructor(props) {
        super(props);

        this.state = {
            messages: Store.getMessages()
        };

        Store.addListener(() => {
            this.setState({
                messages: Store.getMessages()
            });
        });
    }

    render() {
        switch (this.props.size) {
            case 'large':
                return (
                    <Tile title="Mail" href="http://warwick.ac.uk/mymail">
                        <ul className="list-unstyled">
                            {this.state.messages.map((message) => <MailMessageListItem key={message.id} {...message} />)}
                        </ul>
                    </Tile>
                );
            default:
                return (
                    <Tile title="Mail" href="http://warwick.ac.uk/mymail">
                        {this.state.messages.length} unread
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