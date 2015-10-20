const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const TabBar = require('./ui/TabBar');
const MeView = require('./views/MeView');
const NotificationsView = require('./views/NotificationsView');
const ActivityView = require('./views/ActivityView');
const NewsView = require('./views/NewsView');
const SearchView = require('./views/SearchView');

const _ = require('lodash');

import { connect } from 'react-redux';

import { navigate } from '../actions';

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

class Application extends ReactComponent {

    render() {
        const { dispatch, path } = this.props;

        let views = {
            '/': <MeView />,
            '/notifications': <NotificationsView />,
            '/activity': <ActivityView />,
            '/news': <NewsView />,
            '/search': <SearchView />
        };

        return (
            <div>
                {views[path]}
                <TabBar items={TabBarItems} selectedItem={path} onSelectItem={path => dispatch(navigate(path))} />
            </div>
        );
    }

}

function mapStateToProps(state) {
    return {
        path: state.get('path')
    };
}

export default connect(mapStateToProps)(Application);
