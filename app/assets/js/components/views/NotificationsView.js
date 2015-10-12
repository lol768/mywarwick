const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const ActivityItem = require('../ui/ActivityItem');

export default class NotificationsView extends ReactComponent {

    render() {
        return (
            <div>
                <ActivityItem key="a"
                              text="Your submission for CS310 Project Final Report is due tomorrow"
                              source="Tabula"
                              date="2015-10-12T12:00"/>
                <ActivityItem key="b"
                              text="The book '1984' is due back tomorrow"
                              source="Library"
                              date="2015-10-11T09:10"/>
                <ActivityItem key="c"
                              text="The page 'Teaching 15/16' has been updated"
                              source="SiteBuilder"
                              date="2015-09-14T14:36"/>
            </div>
        );
    }

}
