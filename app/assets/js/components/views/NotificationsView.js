const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const ActivityItem = require('../ui/ActivityItem');

const NotificationsStore = require('../../stores/NotificationsStore');

export default class NotificationsView extends ReactComponent {

    constructor(props) {
        super(props);

        this.state = {
            notifications: NotificationsStore.getNotifications()
        };

        NotificationsStore.addListener(() => {
            this.setState({
                notifications: NotificationsStore.getNotifications()
            });
        });
    }

    render() {

        var notificationsList = this.state.notifications.map((notification) => {
            return (
                <ActivityItem key={notification.key}
                              text={notification.text}
                              source={notification.source}
                              date={notification.date}/>
            )
        })

        return (
            <div>
                {notificationsList}
            </div>
        );
    }

}
