const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const ApplicationStore = require('./../stores/ApplicationStore');

const TabBar = require('./ui/TabBar');
const MeView = require('./views/MeView');
const NotificationsView = require('./views/NotificationsView');
const ActivityView = require('./views/ActivityView');
const NewsView = require('./views/NewsView');
const SearchView = require('./views/SearchView');

const TabBarItems = [
    {
        title: 'Me',
        icon: 'user'
    },
    {
        title: 'Notifications',
        icon: 'inbox',
        badge: 3
    },
    {
        title: 'Activity',
        icon: 'dashboard'
    },
    {
        title: 'News',
        icon: 'mortar-board'
    },
    {
        title: 'Search',
        icon: 'search'
    }
];

export default class Application extends ReactComponent {

    constructor(props) {
        super(props);

        this.state = {
            selectedView: 'Me'
        };
    }

    componentDidMount() {
        ApplicationStore.addListener(() => {
            this.setState({
                selectedView: ApplicationStore.getSelectedTab()
            });
        });
    }

    render() {
        let views = {
            Me: <MeView />,
            Notifications: <NotificationsView />,
            Activity: <ActivityView />,
            News: <NewsView />,
            Search: <SearchView />
        };

        return (
            <div>
                {views[this.state.selectedView]}
                <TabBar items={TabBarItems} selectedTab={this.state.selectedView} />
            </div>
        );
    }

}
