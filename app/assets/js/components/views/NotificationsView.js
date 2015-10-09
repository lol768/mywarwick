const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const moment = require('moment');

const GroupedList = require('../ui/GroupedList');
const ActivityItem = require('../ui/ActivityItem');

export default class NotificationsView extends ReactComponent {

    render() {
        return (
            <GroupedList groupBy={groupItemsByDate}>
                <ActivityItem text="Your submission for CS310 Project Final Report is due tomorrow"
                              source="Tabula"
                              date="2015-10-09T00:00"/>
                <ActivityItem text="The book '1984' is due back tomorrow"
                              source="Library"
                              date="2015-10-08T09:00"/>
            </GroupedList>
        );
    }

}

// A way to describe a grouping strategy
let groupItemsByDate = {

    // Describe how things are being grouped
    // Exposed as a CSS class on the grouped list
    description: 'by-date',

    // Which group an item belongs in
    // Return an arbitrary identifier that is the same for all items in the same group
    groupForItem(item) {
        return moment(item.props.date).format('YYYY-MM-DD');
    },

    // The title to be displayed for items in the group
    // Return a nice title for the user to look at, from the group identifier
    titleForGroup(group) {
        return moment(group).calendar();
    }

};
