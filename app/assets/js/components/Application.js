import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import MeView from './views/MeView';
import NotificationsView from './views/NotificationsView';
import ActivityView from './views/ActivityView';
import NewsView from './views/NewsView';
import SearchView from './views/SearchView';
import Immutable from 'immutable';

import _ from 'lodash';

import { connect } from 'react-redux';

import { navigate } from '../actions';

import { getStreamSize } from '../stream';

import { mq } from 'modernizr';

let isDesktop = () => mq('only all and (min-width: 768px)');
import $ from 'jquery';

import store from '../store';

import { registerReducer } from '../reducers';

let initialState = Immutable.Map();
registerReducer('ui', (state = initialState, action) => {
  switch (action.type) {
    case 'ui.class':
      return state.merge({className: action.name});
    default:
      return state;
  }
});

var wasDesktop = isDesktop();
store.dispatch({
  type: 'ui.class',
  name: wasDesktop ? 'desktop' : 'mobile'
});

$(() => {
  $(window).on('resize', () => {
    if (wasDesktop != isDesktop()) {
      wasDesktop = isDesktop();
      store.dispatch({
        type: 'ui.class',
        name: wasDesktop ? 'desktop' : 'mobile'
      });
    }
  });
});

class Application extends ReactComponent {

  render() {
    const { dispatch, path, notificationsCount, layoutClassName } = this.props;

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
        { layoutClassName == 'mobile' ?
          <TabBar selectedItem={path} onSelectItem={path => dispatch(navigate(path))}>
            <TabBarItem title="Me" icon="user" path="/"/>
            <TabBarItem title="Notifications" icon="inbox" path="/notifications" badge={notificationsCount}/>
            <TabBarItem title="Activity" icon="dashboard" path="/activity"/>
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
    notificationsCount: getStreamSize(state.get('notifications')),
    layoutClassName: state.get('ui').get('className')
  };
}

export default connect(mapStateToProps)(Application);
