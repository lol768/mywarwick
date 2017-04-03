import React from 'react';
import { Provider } from 'react-redux';
import { Router, Route, IndexRoute, IndexRedirect } from 'react-router';

import store from '../store';

import AppLayout from './AppLayout';
import NewsView from './views/NewsView';
import MeView from './views/MeView';
import TileView from './views/TileView';
import ActivityView from './views/ActivityView';
import NotificationsView from './views/NotificationsView';
import SearchView from './views/SearchView';

function MaybeEditableMeView(props) {
  if (location.pathname === '/edit') {
    return <MeView editing {...props} />;
  }

  return <MeView editing={false} {...props} />;
}

const AppRoot = ({ history }) => (
  <Provider store={store}>
    <Router history={history}>
      <Route path="/" component={AppLayout}>
        <IndexRoute component={MaybeEditableMeView} />
        <Route path="edit" component={MaybeEditableMeView} />;
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
);

AppRoot.propTypes = {
  history: React.PropTypes.object,
};

export default AppRoot;
