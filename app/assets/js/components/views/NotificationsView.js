const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const moment = require('moment');

const GroupedList = require('../ui/GroupedList');
const ActivityItem = require('../ui/ActivityItem');

export default class NotificationsView extends ReactComponent {

    render() {
        return (
            <GroupedList groupBy={groupItemsByDate}>
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
        var date = moment(item.props.date).startOf('day');

        if (date.isSame(moment(), 'day')) {
            return 'Today';
        } else if (date.isSame(moment().subtract(1, 'day'), 'day')) {
            return 'Yesterday';
        } else if (date.isSame(moment(), 'week')) {
            return 'This Week';
        } else if (date.isSame(moment().subtract(1, 'week'), 'week')) {
            return 'Last Week';
        } else {
            return 'Older';
        }
    },

    // The title to be displayed for items in the group
    // Return a nice title for the user to look at, from the group identifier
    titleForGroup(group) {
        return group;
    }

};
