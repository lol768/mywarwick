import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import MeView from './views/MeView';
import NotificationsView from './views/NotificationsView';
import ActivityView from './views/ActivityView';
import NewsView from './views/NewsView';
import SearchView from './views/SearchView';

import _ from 'lodash';

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
        notificationsCount: state.get('notifications').getPartition(0).length
    };
}

export default connect(mapStateToProps)(Application);
