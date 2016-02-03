import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import MeView from './views/MeView';
import NotificationsView from './views/NotificationsView';
import ActivityView from './views/ActivityView';
import NewsView from './views/NewsView';
import SearchView from './views/SearchView';
import { SettingsView } from './views/SettingsView';
import Immutable from 'immutable';

import { connect } from 'react-redux';

import { navigate } from '../navigate';

import { getNumItemsSince } from '../stream';

import { mq } from 'modernizr';

const isDesktop = () => mq('only all and (min-width: 768px)');
import $ from 'jquery';

import store from '../store';

import { registerReducer } from '../reducers';

const initialState = Immutable.fromJS({
  colourTheme: 'default',
});

registerReducer('ui', (state = initialState, action) => {
  switch (action.type) {
    case 'ui.class':
      return state.set('className', action.className);
    case 'ui.theme':
      return state.set('colourTheme', action.theme);
    default:
      return state;
  }
});

export function updateColourTheme(theme) {
  return {
    type: 'ui.theme',
    theme,
  };
}

export function updateLayoutClass() {
  return {
    type: 'ui.class',
    className: isDesktop() ? 'desktop' : 'mobile',
  };
}

let wasDesktop = isDesktop();
store.dispatch(updateLayoutClass());

$(() => {
  $(window).on('resize', () => {
    if (wasDesktop !== isDesktop()) {
      wasDesktop = isDesktop();
      store.dispatch(updateLayoutClass());
    }
  });
});

class Application extends ReactComponent {

  render() {
    const { dispatch, path, notificationsCount, activitiesCount, layoutClassName, user }
      = this.props;

    const views = {
      '/': <MeView />,
      '/notifications': <NotificationsView grouped />,
      '/activity': <ActivityView grouped />,
      '/news': <NewsView />,
      '/search': <SearchView />,
      '/settings': <SettingsView />,
    };

    return (
      <div>
        {views[path]}
        { layoutClassName === 'mobile' ?
          <TabBar selectedItem={path} onSelectItem={p => dispatch(navigate(p))}>
            <TabBarItem title="Me" icon="user" path="/"/>
            <TabBarItem
              title="Notifications" icon="inbox" path="/notifications"
              badge={ notificationsCount } isDisabled = { !user.authenticated }
            />
            <TabBarItem
              title="Activity" icon="dashboard" path="/activity" badge={ activitiesCount }
              isDisabled = { !user.authenticated }
            />
            <TabBarItem title="News" icon="mortar-board" path="/news"/>
            <TabBarItem title="Search" icon="search" path="/search"/>
          </TabBar>
          : null}
      </div>
    );
  }

}

function mapStateToProps(state) {
  return {
    path: state.get('path'),
    notificationsCount:
      getNumItemsSince(state.get('notifications'), state.get('notifications-metadata').lastRead),
    activitiesCount:
      getNumItemsSince(state.get('activities'), state.get('activities-metadata').lastRead),
    layoutClassName: state.get('ui').get('className'),
    user: state.getIn(['user', 'data']).toJS(),
  };
}

export default connect(mapStateToProps)(Application);
