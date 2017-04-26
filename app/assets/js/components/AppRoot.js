import React from 'react';
import { Provider } from 'react-redux';
import { IndexRedirect, IndexRoute, Route, Router } from 'react-router';

import store from '../store';

import AppLayout from './AppLayout';
import NewsView from './views/NewsView';
import MeView from './views/MeView';
import TileView from './views/TileView';
import ActivityView from './views/ActivityView';
import NotificationsView from './views/NotificationsView';
import SearchView from './views/SearchView';
import AddingTilesView from './views/AddingTilesView';

export const Routes = {
  EDIT: 'edit',
  ADD: 'add',
  TILES: 'tiles',
  NOTIFICATIONS: 'notifications',
  ACTIVITY: 'activity',
  NEWS: 'news',
  SEARCH: 'search',
};

function MaybeEditableMeView(props) {
  const editing = props.route.path === Routes.EDIT;

  return <MeView editing={editing} {...props} />;
}

MaybeEditableMeView.propTypes = {
  route: React.PropTypes.shape({
    path: React.PropTypes.string, // not initially set
  }).isRequired,
};

const AppRoot = ({ history }) => (
  <Provider store={store}>
    <Router history={history}>
      <Route path="/" component={AppLayout}>
        <IndexRoute component={MaybeEditableMeView} />
        <Route path={Routes.EDIT} component={MaybeEditableMeView} />
        <Route path={`${Routes.EDIT}/${Routes.ADD}`} component={AddingTilesView} />
        <Route path={Routes.TILES} component={MaybeEditableMeView}>
          <IndexRedirect to="/" />
        </Route>
        <Route path={`${Routes.TILES}/:id`} component={TileView} />
        <Route path={Routes.NOTIFICATIONS} component={NotificationsView} />
        <Route path={Routes.ACTIVITY} component={ActivityView} />
        <Route path={Routes.NEWS} component={NewsView} />
        <Route path={Routes.SEARCH} component={SearchView} />
      </Route>
    </Router>
  </Provider>
);

AppRoot.propTypes = {
  history: React.PropTypes.object,
};

export default AppRoot;
