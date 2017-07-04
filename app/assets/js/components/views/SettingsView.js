import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { push } from 'react-router-redux';
import { Routes } from '../AppRoot';
import HideableView from './HideableView';
import * as newsCategories from '../../state/news-categories';
import * as newsOptIn from '../../state/news-optin';
import * as emailNotificationsOptIn from '../../state/email-notifications-opt-in';
import { loadDeviceDetails } from '../../userinfo';
import _ from 'lodash-es';
import CheckboxListGroupItem from '../ui/CheckboxListGroupItem';


class SettingsView extends HideableView {

  constructor(props) {
    super(props);
    this.state = {
      emailNotificationsOptIn: {
        wantsEmails: props.emailNotificationsOptIn.wantsEmails,
        fetchedOnce: false,
      },
    };
    this.onNotificationEmailCopyChange = this.onNotificationEmailCopyChange.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(nextProps);
  }

  static propTypes = {
    mutes: PropTypes.number.isRequired,
    newsCategories: PropTypes.shape({
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      fetched: PropTypes.bool.isRequired,
      selected: PropTypes.number.isRequired,
      total: PropTypes.number.isRequired,
    }).isRequired,
    emailNotificationsOptIn: PropTypes.shape({
      fetchedOnce: PropTypes.bool.isRequired,
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      wantsEmails: PropTypes.bool.isRequired,
    }),
    newsOptIn: PropTypes.shape({
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      fetched: PropTypes.bool.isRequired,
      location: PropTypes.shape({
        selected: PropTypes.number.isRequired,
        total: PropTypes.number.isRequired,
      }).isRequired,
    }).isRequired,
    activityFilter: PropTypes.shape({
      selected: PropTypes.number.isRequired,
      total: PropTypes.number.isRequired,
    }).isRequired,
    notificationFilter: PropTypes.shape({
      selected: PropTypes.number.isRequired,
      total: PropTypes.number.isRequired,
    }).isRequired,
    dispatch: PropTypes.func.isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  static renderSetting(icon, title, rightView) {
    return (
      <div className="media">
        <div className="media-left">
          <i className={ `fa fa-fw fa-${icon}` } />
        </div>
        <div className="media-body">
          { title }
        </div>
        <div className="media-right">
          { rightView }
        </div>
      </div>
    );
  }

  static renderSingleCount(number) {
    return (
      <div>
        <span className={ classNames({ 'badge progress-bar-danger': number > 0 }) }>
          { number }
        </span>
        <i className="fa fa-fw fa-chevron-right" />
      </div>
    );
  }

  static renderFractionCount(number, total) {
    const fraction = (number === total) ? 'All' : `${number}/${total}`;
    return (
      <div>
        { fraction }
        <i className="fa fa-fw fa-chevron-right" />
      </div>
    );
  }

  static renderFetchedCount(props) {
    const { fetching, failed, selected, total, fetched } = props;
    if (fetching) {
      return (
        <div>
          <i className="fa fa-spinner fa-pulse" />
          <i className="fa fa-fw fa-chevron-right" />
        </div>
      );
    } else if ((failed && fetched) || !fetched) {
      return (
        <div>
          <i className="fa fa-exclamation-circle text-danger" />
          <i className="fa fa-fw fa-chevron-right" />
        </div>
      );
    }
    return SettingsView.renderFractionCount(selected, total);
  }

  componentDidShow() {
    if (this.props.isOnline) {
      this.props.dispatch(newsCategories.fetch());
      this.props.dispatch(newsOptIn.fetch());
    }
    this.props.dispatch(emailNotificationsOptIn.fetch());
  }

  static getNativeAppVersion() {
    if ('MyWarwickNative' in window && 'getAppVersion' in window.MyWarwickNative) {
      return window.MyWarwickNative.getAppVersion();
    }

    return null;
  }

  getVersionString() {
    const { assetsRevision } = this.props;
    const userAgent = window.navigator.userAgent;
    const appVersion = SettingsView.getNativeAppVersion();

    const versions = [];

    if (appVersion !== null) {
      if (userAgent.indexOf('Android') >= 0) {
        versions.push(`Android ${appVersion}`);
      } else if (/iPad|iPhone|iPod/.test(userAgent)) {
        versions.push(`iOS ${appVersion}`);
      } else {
        versions.push(`App ${appVersion}`);
      }
    }

    if (assetsRevision !== null && assetsRevision !== undefined) {
      versions.push(`Web ${assetsRevision}`);
    }

    return versions.join(', ');
  }

  onNotificationEmailCopyChange() {
    const consensus = !this.props.emailNotificationsOptIn.wantsEmails;

    this.setState((previousState) => {
      const newState = Object.assign({}, previousState.emailNotificationsOptIn);
      newState.wantsEmails = consensus;
      return { emailNotificationsOptIn: newState };
    });

    this.props.dispatch(
      emailNotificationsOptIn.persist(
        consensus
      )
    );
  }

  render() {
    return (
      <div>
        <div className="list-group fixed">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Settings</h3>
            </div>
          </div>
        </div>

        <div className="list-group setting-colour-0">
          <div className="list-group-item"
            onClick={ () =>
              this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.TILES}`))
            }
          >
            { SettingsView.renderSetting(
              'check-square-o',
              'Tile preferences',
              <i className="fa fa-fw fa-chevron-right" />
            ) }
          </div>
        </div>

        <div className="list-group setting-colour-0">
          <div className="list-group-item"
            onClick={ () =>
              this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.MUTES}`))
            }
          >
            { SettingsView.renderSetting(
              'bell-slash-o',
              'Muted notifications',
              SettingsView.renderSingleCount(this.props.mutes)
            ) }
          </div>
          <CheckboxListGroupItem id="copyNotificationsEmail"
            value
            icon="envelope"
            description="Copy my notifications to email"
            onClick={ this.onNotificationEmailCopyChange }
            checked={ this.state.emailNotificationsOptIn.wantsEmails }
            failure={ this.state.emailNotificationsOptIn.failed && !this.props.isOnline}
            loading={ !this.state.emailNotificationsOptIn.fetchedOnce &&
              this.props.emailNotificationsOptIn.fetching }
            disabled={ !this.props.isOnline }
          />
        </div>

        <div className="list-group setting-colour-1">
          <div className="list-group-item"
            onClick={ this.props.newsCategories.fetched && !this.props.newsCategories.failed ? () =>
              this.props.dispatch(
                push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NEWS_CATEGORIES}`)
              ) : null
            }
          >
            { SettingsView.renderSetting(
              'newspaper-o',
              'News categories',
              SettingsView.renderFetchedCount({
                ...this.props.newsCategories,
              })
            ) }
          </div>
          <div className="list-group-item"
            onClick={ this.props.newsOptIn.fetched && !this.props.newsOptIn.failed ? () =>
              this.props.dispatch(
                push(
                  `/${Routes.SETTINGS}/${Routes.SettingsRoutes.OPT_IN}/` +
                  `${Routes.SettingsRoutes.OptInTypes.LOCATION}`
                )
              ) : null
            }
          >
            { SettingsView.renderSetting(
              'map-signs',
              'Location preferences',
              SettingsView.renderFetchedCount({
                fetching: this.props.newsOptIn.fetching,
                failed: this.props.newsOptIn.failed,
                fetched: this.props.newsOptIn.fetched,
                selected: this.props.newsOptIn.location.selected,
                total: this.props.newsOptIn.location.total,
                isOnline: this.props.isOnline,
              })
            ) }
          </div>
        </div>

        <div className="list-group setting-colour-2">
          <div className="list-group-item"
            onClick={ () =>
              this.props.dispatch(
                push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.ACTIVITY_FILTER}`)
              )
            }
          >
            { SettingsView.renderSetting(
              'dashboard',
              'Activity filter',
              SettingsView.renderFractionCount(
                this.props.activityFilter.selected,
                this.props.activityFilter.total
              )
            ) }
          </div>
          <div className="list-group-item"
            onClick={ () =>
              this.props.dispatch(
                push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NOTIFICATION_FILTER}`)
              )
            }
          >
            { SettingsView.renderSetting(
              'bell-o',
              'Notifications filter',
              SettingsView.renderFractionCount(
                this.props.notificationFilter.selected,
                this.props.notificationFilter.total
              )
            ) }
          </div>
        </div>

        <div className="list-group setting-colour-3">
          <div className="list-group-item" onClick={ loadDeviceDetails }>
            <div className="media">
              <div className="media-left feedback">
                <span className="fa-stack">
                  <i className="fa fa-fw fa-comment-o fa-stack-2x" />
                  <strong className="fa-fw fa-stack-1x">!</strong>
                </span>
              </div>
              <div className="media-body">
                Send feedback
              </div>
              <div className="media-right">
                <i className="fa fa-fw fa-chevron-right" />
              </div>
            </div>
          </div>
          <div className="list-group-item">
            { SettingsView.renderSetting('info-circle', this.getVersionString(), null) }
          </div>
        </div>
      </div>
    );
  }
}

// Remove from the user's filter choice any keys that are not present in the filter options
function ensureValidFilterChoice(filter, filterOptions) {
  return _.mapValues(filter, (options, optionType) =>
    _.pickBy(options, (v, option) =>
      filterOptions[optionType] !== undefined &&
      _.find(filterOptions[optionType], filterOption =>
        filterOption.id === option
      ) !== undefined
    )
  );
}

const select = (state) => {
  const activityFilterTotal = _.reduce(
    state.activities.filterOptions,
    (total, o) => total + o.length,
    0
  );
  const notificationFilterTotal = _.reduce(
    state.notifications.filterOptions,
    (total, o) => total + o.length,
    0
  );

  return {
    mutes: state.notifications.activityMutes.length,
    subscribedNewsCategories: state.newsCategories.subscribed.length,
    emailNotificationsOptIn: {
      wantsEmails: state.emailNotificationsOptIn.wantsEmails,
      fetchedOnce: state.emailNotificationsOptIn.fetchedOnce,
      fetching: state.emailNotificationsOptIn.fetching,
      failed: state.emailNotificationsOptIn.failed,
    },
    newsCategories: {
      fetching: state.newsCategories.fetching,
      failed: state.newsCategories.failed,
      fetched: state.newsCategories.fetched,
      selected: state.newsCategories.subscribed.length,
      total: state.newsCategories.items.length,
    },
    newsOptIn: {
      fetching: state.newsOptIn.fetching,
      failed: state.newsOptIn.failed,
      fetched: state.newsOptIn.fetched,
      location: {
        selected: (state.newsOptIn.selected.Location || []).length,
        total: (state.newsOptIn.options.Location || []).length,
      },
    },
    activityFilter: {
      selected: activityFilterTotal - _.reduce(
        ensureValidFilterChoice(state.activities.filter, state.activities.filterOptions),
        (total, o) => total + _.filter(o, v => !v).length,
        0
      ),
      total: activityFilterTotal,
    },
    notificationFilter: {
      selected: notificationFilterTotal - _.reduce(
        ensureValidFilterChoice(state.notifications.filter, state.notifications.filterOptions),
        (total, o) => total + _.filter(o, v => !v).length,
        0
      ),
      total: notificationFilterTotal,
    },
    assetsRevision: state.app.assets.revision,
    isOnline: state.device.isOnline,
  };
};

export default connect(select)(SettingsView);
