const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const TabBar = require('./ui/TabBar');
import TabBarItem from './ui/TabBarItem';
const MeView = require('./views/MeView');
const NotificationsView = require('./views/NotificationsView');
const ActivityView = require('./views/ActivityView');
const NewsView = require('./views/NewsView');
const SearchView = require('./views/SearchView');

const _ = require('lodash');

import { connect } from 'react-redux';

import { navigate } from '../actions';

class Application extends ReactComponent {

    render() {
        const { dispatch, path, notificationsCount } = this.props;

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
                <TabBar selectedItem={path} onSelectItem={path => dispatch(navigate(path))}>
                    <TabBarItem title="Me" icon="user" path="/" />
                    <TabBarItem title="Notifications" icon="inbox" path="/notifications" badge={notificationsCount} />
                    <TabBarItem title="Activity" icon="dashboard" path="/activity" />
                    <TabBarItem title="News" icon="mortar-board" path="/news" />
                    <TabBarItem title="Search" icon="search" path="/search" />
                </TabBar>
            </div>
        );
    }

}

function mapStateToProps(state) {
    return {
        path: state.get('path'),
        notificationsCount: state.get('notifications').count()
    };
}

export default connect(mapStateToProps)(Application);
