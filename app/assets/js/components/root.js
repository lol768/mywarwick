import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { Router, Route, IndexRoute, IndexRedirect } from 'react-router';

import store from '../store';

import Application from './Application';
import NewsView from './views/NewsView';
import MeView from './views/MeView';
import TileView from './views/TileView';
import ActivityView from './views/ActivityView';
import NotificationsView from './views/NotificationsView';
import SearchView from './views/SearchView';

export default function root() {
  return (
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" component={Application}>
          <IndexRoute component={MeView} />
          <Route path="tiles" component={MeView}>
            <IndexRedirect to="/" />
            <Route path=":id" component={TileView} />
          </Route>
          <Route path="notifications" component={NotificationsView} />
          <Route path="activity" component={ActivityView} />
          <Route path="news" component={NewsView} />
          <Route path="search" component={SearchView} />
        </Route>
      </Router>
    </Provider>
  )
}