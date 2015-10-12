const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const ApplicationStore = require('./../stores/ApplicationStore');

const TabBar = require('./ui/TabBar');
const MeView = require('./views/MeView');
const NotificationsView = require('./views/NotificationsView');
const ActivityView = require('./views/ActivityView');
const NewsView = require('./views/NewsView');
const SearchView = require('./views/SearchView');

const _ = require('lodash');

const TabBarItems = [
    {
        title: 'Me',
        icon: 'user',
        path: '/'
    },
    {
        title: 'Notifications',
        icon: 'inbox',
        badge: 3,
        path: '/notifications'
    },
    {
        title: 'Activity',
        icon: 'dashboard',
        path: '/activity'
    },
    {
        title: 'News',
        icon: 'mortar-board',
        path: '/news'
    },
    {
        title: 'Search',
        icon: 'search',
        path: '/search'
    }
];

export default class Application extends ReactComponent {

    constructor(props) {
        super(props);

        this.state = {
            path: ApplicationStore.getCurrentPath()
        };
    }

    componentDidMount() {
        ApplicationStore.addListener(() => {
            let path = ApplicationStore.getCurrentPath();

            this.setState({
                path: path
            });

            if (window.history.pushState && window.location.pathname != path) {
                window.history.pushState(null, null, path);
            }
        });
    }

    render() {
        let views = {
            '/': <MeView />,
            '/notifications': <NotificationsView />,
            '/activity': <ActivityView />,
            '/news': <NewsView />,
            '/search': <SearchView />
        };

        return (
            <div>
                {views[this.state.path]}
                <TabBar items={TabBarItems} selectedItem={this.state.path} />
            </div>
        );
    }

}
