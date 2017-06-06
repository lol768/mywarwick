import React from 'react';
import NewsView from './views/NewsView';
import MeView from './views/MeView';
import TileView from './views/TileView';
import ActivityView from './views/ActivityView';
import NotificationsView from './views/NotificationsView';
import SearchView from './views/SearchView';
import AddingTilesView from './views/AddingTilesView';
import AppLayout from './AppLayout';
import * as _ from 'lodash-es';
import { goBack, replace } from 'react-router-redux';
import { connect } from 'react-redux';
import ActivityMutesView from './views/ActivityMutesView';

export const Routes = {
  EDIT: 'edit',
  ADD: 'add',
  TILES: 'tiles',
  NOTIFICATIONS: 'notifications',
  MUTE: 'mute',
  ACTIVITY: 'activity',
  NEWS: 'news',
  SEARCH: 'search',
};

const RouteViews = {};
RouteViews['/'] = {
  rendered: false,
  view: MeView,
};
RouteViews[`/${Routes.EDIT}`] = {
  rendered: false,
  view: MeView,
  extraProps: {
    editing: true,
  },
};
RouteViews[`/${Routes.EDIT}/${Routes.ADD}`] = {
  rendered: false,
  view: AddingTilesView,
};
RouteViews[`/${Routes.NOTIFICATIONS}`] = {
  rendered: false,
  view: NotificationsView,
};
RouteViews[`/${Routes.NOTIFICATIONS}/${Routes.MUTE}`] = {
  rendered: false,
  view: ActivityMutesView,
};
RouteViews[`/${Routes.ACTIVITY}`] = {
  rendered: false,
  view: ActivityView,
};
RouteViews[`/${Routes.NEWS}`] = {
  rendered: false,
  view: NewsView,
};
RouteViews[`/${Routes.SEARCH}`] = {
  rendered: false,
  view: SearchView,
};

class AppRoot extends React.Component {

  static propTypes = {
    history: React.PropTypes.object.isRequired,
    navRequest: React.PropTypes.string,
    dispatch: React.PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);

    this.shouldRender = this.shouldRender.bind(this);
    this.expandedTile = this.expandedTile.bind(this);

    this.state = {
      location: window.location,
    };
  }

  componentDidMount() {
    this.historyUnlisten = this.props.history.listen(location => this.setState({ location }));
  }

  componentWillUnmount() {
    this.historyUnlisten();
  }

  /**
   * The other half of this is in ui.navRequest.
   * If we've been sent back only to go forward, get the requested path from the store, reset
   * that state, and replace the current path.
   */
  componentDidUpdate(prevProps, prevState) {
    if (this.props.navRequest && prevState.location.pathname !== this.state.location.pathname) {
      if (
        this.state.location.pathname === '/' ||
          this.state.location.pathname === `/${Routes.NOTIFICATIONS}`
      ) {
        const path = this.props.navRequest;
        this.props.dispatch({
          type: 'ui.navRequest',
          navRequest: null,
        });
        this.props.dispatch(replace(path));
      } else {
        this.props.dispatch(goBack());
      }
    }
  }

  shouldRender(path) {
    if (this.state.location.pathname === path) {
      RouteViews[path].rendered = true;
    }
    return RouteViews[path].rendered;
  }

  expandedTile() {
    return /^\/tiles\/(.+)/.exec(this.state.location.pathname);
  }

  render() {
    const { location } = this.state;
    const tilePath = this.expandedTile();

    const views = _.map(RouteViews, (args, path) => (
      this.shouldRender(path) ?
        <div key={ path } className={(location.pathname !== path) ? 'hidden' : ''}>
          {
            React.createElement(
              args.view,
              Object.assign(
                {},
                this.props,
                {
                  hiddenView: location.pathname !== path,
                  location,
                },
                (args.extraProps || {})
              )
            )
          }
        </div> : null
    ));

    if ((tilePath || []).length === 2) {
      views.push(
        <TileView
          id={ tilePath[1] } params={{ id: tilePath[1] }}
          hiddenView={ false }
          {...this.props}
        />
      );
    } else {
      views.push(null);
    }

    return (
      <AppLayout {...this.props} location={ location }>
        { views }
      </AppLayout>
    );
  }
}

const select = (state) => ({
  navRequest: state.ui.navRequest,
});

export default connect(select)(AppRoot);
