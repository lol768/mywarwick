/* eslint-env browser */

import React from 'react';
import * as PropTypes from 'prop-types';
import * as _ from 'lodash-es';
import { goBack, replace } from 'react-router-redux';
import { connect } from 'react-redux';
import NewsView from './views/NewsView';
import MeView from './views/MeView';
import TileView from './views/TileView';
import ActivityView from './views/ActivityView';
import NotificationsView from './views/NotificationsView';
import SearchView from './views/SearchView';
import AddingTilesView from './views/AddingTilesView';
import AppLayout from './AppLayout';
import ActivityMutesView from './views/settings/ActivityMutesView';
import Visible from './Visible';
import SettingsView from './views/SettingsView';
import NewsCategoriesView from './views/settings/NewsCategoriesView';
import OptInSettingsView from './views/settings/OptInSettingsView';
import LocationOptInSettingView from './views/settings/optInSettings/LocationOptInSettingView';
import TilePreferencesView from './views/settings/TilePreferencesView';
import ColourSchemesView from './views/settings/ColourSchemesView';
import TileOptionView from './views/settings/TileOptionView';
import PostTourView from './views/PostTourView';
import TimetableAlarmsView from './views/settings/TimetableAlarmsView';
import DoNotDisturbView from './views/settings/DoNotDisturbView';
import {
  ActivityStreamFilterOptionView,
  NotificationStreamFilterOptionView,
} from './views/settings/StreamFilterOptionView';
import SmsNotificationsView from './views/settings/SmsNotificationsView';
import EAPPreferenceView from './views/settings/EAPPreferenceView';

export const Routes = {
  EDIT: 'edit',
  ADD: 'add',
  TILES: 'tiles',
  NOTIFICATIONS: 'alerts',
  ACTIVITY: 'activity',
  NEWS: 'news',
  SEARCH: 'search',
  SETTINGS: 'settings',
  SettingsRoutes: {
    TILES: 'tiles',
    COLOUR_SCHEMES: 'colourschemes',
    MUTES: 'mutes',
    NEWS_CATEGORIES: 'newscategories',
    OPT_IN: 'optin',
    OptInTypes: {
      LOCATION: 'location',
    },
    ACTIVITY_FILTER: 'activityfilter',
    NOTIFICATION_FILTER: 'notificationfilter',
    SMS: 'sms',
    TIMETABLE_ALARMS: 'timetable_alarms',
    DO_NOT_DISTURB: 'do_not_disturb',
    EAP: 'eap',
  },
  POST_TOUR: 'post_tour',
};

const TabRoutes = [
  '/',
  `/${Routes.NOTIFICATIONS}`,
  `/${Routes.ACTIVITY}`,
  `/${Routes.NEWS}`,
  `/${Routes.SEARCH}`,
];

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
RouteViews['/notifications'] = {
  rendered: false,
  view: NotificationsView,
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
RouteViews[`/${Routes.SETTINGS}`] = {
  rendered: false,
  view: SettingsView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.TILES}`] = {
  rendered: false,
  view: TilePreferencesView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.COLOUR_SCHEMES}`] = {
  rendered: false,
  view: ColourSchemesView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.MUTES}`] = {
  rendered: false,
  view: ActivityMutesView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NEWS_CATEGORIES}`] = {
  rendered: false,
  view: NewsCategoriesView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.OPT_IN}`] = {
  rendered: false,
  view: OptInSettingsView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.DO_NOT_DISTURB}`] = {
  rendered: false,
  view: DoNotDisturbView,
};
RouteViews[`/${Routes.POST_TOUR}`] = {
  rendered: false,
  view: PostTourView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.ACTIVITY_FILTER}`] = {
  rendered: false,
  view: ActivityStreamFilterOptionView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NOTIFICATION_FILTER}`] = {
  rendered: false,
  view: NotificationStreamFilterOptionView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.SMS}`] = {
  rendered: false,
  view: SmsNotificationsView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.TIMETABLE_ALARMS}`] = {
  rendered: false,
  view: TimetableAlarmsView,
};
RouteViews[`/${Routes.SETTINGS}/${Routes.SettingsRoutes.EAP}`] = {
  rendered: false,
  view: EAPPreferenceView,
};

class AppRoot extends React.Component {
  static propTypes = {
    history: PropTypes.object.isRequired,
    navRequest: PropTypes.string,
    newsOptInOptions: PropTypes.shape({
      options: PropTypes.objectOf(PropTypes.arrayOf(PropTypes.shape({
        value: PropTypes.string.isRequired,
        description: PropTypes.string.isRequired,
      }))).isRequired,
      selected: PropTypes.objectOf(PropTypes.arrayOf(PropTypes.string)).isRequired,
    }).isRequired,
    tileData: PropTypes.shape({
      tiles: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired,
        colour: PropTypes.number.isRequired,
        icon: PropTypes.string.isRequired,
        title: PropTypes.string.isRequired,
        preferences: PropTypes.object,
      })),
      options: PropTypes.object.isRequired,
    }).isRequired,
    dispatch: PropTypes.func.isRequired,
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

  /**
   * The other half of this is in ui.navRequest.
   * If we've been sent back only to go forward, get the requested path from the store, reset
   * that state, and replace the current path.
   */
  componentDidUpdate(prevProps, prevState) {
    if (this.props.navRequest && prevState.location.pathname !== this.state.location.pathname) {
      if (_.includes(TabRoutes, this.state.location.pathname)) {
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

  componentWillUnmount() {
    this.historyUnlisten();
  }

  shouldRender(path) {
    if (this.state.location.pathname === path) {
      RouteViews[path].rendered = true;
    }
    return RouteViews[path].rendered;
  }

  expandedTile() {
    return new RegExp(`^/${Routes.TILES}/([^\\?/]+)[\\?/]*.*`).exec(this.state.location.pathname);
  }

  tileOption() {
    return new RegExp(`^/${Routes.SETTINGS}/${Routes.SettingsRoutes.TILES}/(.+)`).exec(
      this.state.location.pathname,
    );
  }

  singleOptInSetting() {
    return new RegExp(`^/${Routes.SETTINGS}/${Routes.SettingsRoutes.OPT_IN}/(.+)`).exec(
      this.state.location.pathname,
    );
  }

  render() {
    const { location } = this.state;
    const tilePath = this.expandedTile();
    const tileOptionPath = this.tileOption();
    const optInPath = this.singleOptInSetting();

    const views = _.map(RouteViews, (args, path) => (
      this.shouldRender(path) ?
        <Visible key={ path } visible={ location.pathname === path }>
          {
            React.createElement(
              args.view,
              args.extraProps || {},
            )
          }
        </Visible> : null
    ));

    if ((tilePath || []).length >= 2) {
      const queryParams = (this.state.location.search.length > 0) ?
        _.fromPairs(
          _.compact(
            _.map(
              this.state.location.search.substr(1).split('&'),
              term => term.split('='),
            ),
          ),
        ) : {};
      views.push(
        <Visible key={ tilePath[1] } visible>
          <TileView
            id={ tilePath[1] }
            params={{ id: tilePath[1], queryParams }}
            {...this.props}
          />
        </Visible>,
      );
    } else {
      views.push(null);
    }

    if ((tileOptionPath || []).length === 2 && this.props.tileData.tiles.length > 0) {
      views.push(
        <Visible key={ tileOptionPath[1] } visible>
          <TileOptionView
            tile={ _.find(this.props.tileData.tiles, tile => tile.id === tileOptionPath[1]) }
            tileOptions={ this.props.tileData.options[tileOptionPath[1]] }
            dispatch={ this.props.dispatch }
          />
        </Visible>,
      );
    } else {
      views.push(null);
    }

    if ((optInPath || []).length === 2) {
      switch (optInPath[1]) {
        case Routes.SettingsRoutes.OptInTypes.LOCATION:
          views.push(
            <Visible key={ `OptIn:${optInPath[1]}` } visible>
              <OptInSettingsView
                options={ this.props.newsOptInOptions.options }
                selected={ this.props.newsOptInOptions.selected }
                dispatch={ this.props.dispatch }
                singleOptionView={ LocationOptInSettingView }
                singleOptionIdentifier={ 'Location' }
              />
            </Visible>,
          );
          break;
        default:
          views.push(null);
          break;
      }
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

const select = state => ({
  navRequest: state.ui.navRequest,
  newsOptInOptions: state.newsOptIn,
  tileData: state.tiles.data,
});

export default connect(select)(AppRoot);
