/* eslint-env browser */

import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { push } from 'react-router-redux';
import _ from 'lodash-es';

import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import HideableView from './HideableView';
import * as newsCategories from '../../state/news-categories';
import * as newsOptIn from '../../state/news-optin';
import * as emailNotificationsOptIn from '../../state/email-notifications-opt-in';
import * as smsNotifications from '../../state/sms-notifications';
import * as eap from '../../state/eap';
import * as doNotDisturb from '../../state/do-not-disturb';
import { loadDeviceDetails, signOut } from '../../userinfo';
import SwitchListGroupItem from '../ui/SwitchListGroupItem';
import wrapKeyboardSelect from '../../keyboard-nav';
import * as FA from '../FA';

const FAChevronRight = () => <FA.ChevronRight fw />;

const ListGroupItemBtn = props => (
  <div
    className="list-group-item"
    role="button"
    tabIndex={0}
    onClick={props.handler}
    onKeyUp={props.handler}
  >
    {props.children}
  </div>
);

ListGroupItemBtn.propTypes = {
  handler: PropTypes.func.isRequired,
  children: PropTypes.element.isRequired,
};

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
    this.onTilePreferences = this.onTilePreferences.bind(this);
    this.onColourScheme = this.onColourScheme.bind(this);
    this.onNotificationMutes = this.onNotificationMutes.bind(this);
    this.onSMS = this.onSMS.bind(this);
    this.onLocationPreferences = this.onLocationPreferences.bind(this);
    this.onActivityFilter = this.onActivityFilter.bind(this);
    this.onNotificationFilter = this.onNotificationFilter.bind(this);
    this.onEditTiles = this.onEditTiles.bind(this);
    this.onDoNotDisturb = this.onDoNotDisturb.bind(this);
    this.onEAP = this.onEAP.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(nextProps);
  }

  static propTypes = {
    features: PropTypes.object.isRequired,
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
    smsNotifications: PropTypes.shape({
      fetching: PropTypes.bool.isRequired,
      fetched: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      enabled: PropTypes.bool.isRequired,
      smsNumber: PropTypes.string,
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
    colourSchemes: PropTypes.shape({
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      fetched: PropTypes.bool.isRequired,
      chosen: PropTypes.number.isRequired,
      schemes: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.number.isRequired,
        url: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
      })).isRequired,
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
    eap: PropTypes.shape({
      fetched: PropTypes.bool.isRequired,
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      enabled: PropTypes.bool.isRequired,
    }).isRequired,
    doNotDisturbEnabled: PropTypes.bool.isRequired,
  };

  static renderSetting(icon, title, rightView, disabled = false) {
    return (
      <div className="media">
        <div className="media-left">
          <i className={`fal fa-fw fa-${icon}`} />
        </div>
        <div className={`media-body ${disabled ? 'media-body-disabled' : ''}`}>
          {title}
        </div>
        <div className="media-right">
          {rightView}
        </div>
      </div>
    );
  }

  static shouldShowColourSchemes() {
    const native = window.MyWarwickNative; // eslint-disable-line no-undef
    return !native || ('setBackgroundToDisplay' in native);
  }

  static shouldShowTimetableAlarms() {
    return ('MyWarwickNative' in window) && ('setTimetableNotificationsEnabled' in window.MyWarwickNative); // eslint-disable-line no-undef, max-len
  }

  shouldShowDoNotDisturb() {
    return this.props.features.doNotDisturb;
  }

  static renderSingleCount(number) {
    return (
      <div>
        <span className={classNames({ 'badge progress-bar-danger': number > 0 })}>
          {number}
        </span>
        <FAChevronRight />
      </div>
    );
  }

  static renderFractionCount(number, total) {
    const fraction = (number === total) ? 'All' : `${number}/${total}`;
    return (
      <div>
        {fraction}
        <FAChevronRight />
      </div>
    );
  }

  static renderFetchedCount(props) {
    const {
      fetching,
      failed,
      selected,
      total,
      fetched,
    } = props;
    if (fetching) {
      return (
        <div>
          <i className="fal fa-spinner fa-pulse" />
          <FAChevronRight />
        </div>
      );
    }

    if ((failed && fetched) || !fetched) {
      return (
        <div>
          <i className="fal fa-exclamation-circle text-danger" />
          <FAChevronRight />
        </div>
      );
    }

    return SettingsView.renderFractionCount(selected, total);
  }

  static renderFetchedBool(props, enabledLabel = 'Enabled', disabledLabel = 'Disabled') {
    const {
      fetching,
      failed,
      enabled,
      fetched,
    } = props;
    if (fetching) {
      return (
        <div>
          <i className="fal fa-spinner fa-pulse" />
          <FAChevronRight />
        </div>
      );
    }

    if ((failed && fetched) || !fetched) {
      return (
        <div>
          <i className="fal fa-exclamation-circle text-danger" />
          <FAChevronRight />
        </div>
      );
    }

    return (
      <div>
        {(enabled) ? enabledLabel : disabledLabel}
        <FAChevronRight />
      </div>
    );
  }

  componentDidShow() {
    if (this.props.isOnline) {
      this.props.dispatch(newsCategories.fetch());
      this.props.dispatch(newsOptIn.fetch());
      this.props.dispatch(emailNotificationsOptIn.fetch());
      this.props.dispatch(smsNotifications.fetch());
      this.props.dispatch(eap.fetch());
      this.props.dispatch(doNotDisturb.fetch());
    }
  }

  static canLaunchTour() {
    return 'MyWarwickNative' in window && 'launchTour' in window.MyWarwickNative;
  }

  static launchTour(e) {
    wrapKeyboardSelect(() => {
      if (SettingsView.canLaunchTour()) {
        return window.MyWarwickNative.launchTour();
      }
      return null;
    }, e);
  }

  getVersionString() {
    const { assetsRevision, appVersion } = this.props;
    const { userAgent } = window.navigator;

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
    this.setState((previousState) => {
      const consensus = !previousState.emailNotificationsOptIn.wantsEmails;

      const newState = Object.assign({}, previousState.emailNotificationsOptIn);
      newState.wantsEmails = consensus;

      this.props.dispatch(
        emailNotificationsOptIn.persist(
          consensus,
        ),
      );

      return { emailNotificationsOptIn: newState };
    });
  }

  chosenSchemeName() {
    const { colourSchemes } = this.props;
    const { fetching, failed, fetched } = colourSchemes;

    if (fetching) {
      return (
        <div>
          <i className="fal fa-spinner fa-pulse" />
          <FAChevronRight />
        </div>
      );
    }

    if ((failed && fetched) || !fetched) {
      return (
        <div>
          <i className="fal fa-exclamation-circle text-danger" />
          <FAChevronRight />
        </div>
      );
    }

    const condition = e => e.id === colourSchemes.chosen;
    const chosenSchemeName = _.find(colourSchemes.schemes, condition).name;
    return (
      <div>
        <span className="colour-scheme__current">
          {chosenSchemeName}
        </span>
        <FAChevronRight />
      </div>
    );
  }

  onTilePreferences(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.TILES}`));
    }, e);
  }

  onColourScheme(e) {
    wrapKeyboardSelect(() => {
      if (this.props.isOnline) {
        this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.COLOUR_SCHEMES}`));
      }
    }, e);
  }

  onNotificationMutes(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.MUTES}`));
    }, e);
  }

  onSMS(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.SMS}`));
    }, e);
  }

  onLocationPreferences(e) {
    wrapKeyboardSelect(() => {
      if (this.props.newsOptIn.fetched && !this.props.newsOptIn.failed) {
        this.props.dispatch(
          push(
            `/${Routes.SETTINGS}/${Routes.SettingsRoutes.OPT_IN}/`
            + `${Routes.SettingsRoutes.OptInTypes.LOCATION}`,
          ),
        );
      }
    }, e);
  }

  onActivityFilter(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(
        push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.ACTIVITY_FILTER}`),
      );
    }, e);
  }

  onNotificationFilter(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(
        push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NOTIFICATION_FILTER}`),
      );
    }, e);
  }

  onTimetableAlarms = (e) => {
    wrapKeyboardSelect(() => {
      this.props.dispatch(
        push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.TIMETABLE_ALARMS}`),
      );
    }, e);
  };

  onEditTiles(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(push(`/${Routes.EDIT}`));
    }, e);
  }

  onDoNotDisturb(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.DO_NOT_DISTURB}`));
    }, e);
  }

  onEAP(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.EAP}`));
    }, e);
  }

  static onSendFeedback(e) {
    wrapKeyboardSelect(loadDeviceDetails, e);
  }

  static onWhatsNew(e) {
    wrapKeyboardSelect(() => {
      window.location = 'https://warwick.ac.uk/mw-support/whatsnew';
    }, e);
  }

  static onSignOut(e) {
    wrapKeyboardSelect(signOut, e);
  }

  render() {
    return (
      <ScrollRestore url={`/${Routes.SETTINGS}`}>
        <div>
          <div className="list-group fixed">
            <div className="list-group-item">
              <div className="list-group-item-heading">
                <h3>Settings</h3>
              </div>
            </div>
          </div>

          <div className="list-group setting-colour-0">
            {this.props.features.eap
            && <ListGroupItemBtn handler={this.onEAP}>
              {SettingsView.renderSetting(
                'eye',
                'Early access program',
                SettingsView.renderFetchedBool({
                  ...this.props.eap,
                }, 'On', 'Off'),
              )}
            </ListGroupItemBtn>}
            {this.props.features.updateTileEditUI
            && <ListGroupItemBtn handler={this.onEditTiles}>
              {SettingsView.renderSetting(
                'th-large',
                'Rearrange, resize & show or hide tiles',
                <FAChevronRight />,
              )}
            </ListGroupItemBtn>}
            <ListGroupItemBtn handler={this.onTilePreferences}>
              {SettingsView.renderSetting(
                'check-square',
                'Tile preferences',
                <FAChevronRight />,
              )}
            </ListGroupItemBtn>
            {SettingsView.shouldShowColourSchemes()
            && <ListGroupItemBtn handler={this.onColourScheme}>
              {SettingsView.renderSetting(
                'paint-brush',
                'Colour scheme',
                this.chosenSchemeName(),
                !this.props.isOnline,
              )}
            </ListGroupItemBtn>
            }
          </div>

          <div className="list-group setting-colour-0">
            <ListGroupItemBtn handler={this.onNotificationMutes}>
              {SettingsView.renderSetting(
                'bell-slash',
                'Muted alerts',
                SettingsView.renderSingleCount(this.props.mutes),
              )}
            </ListGroupItemBtn>
            {this.shouldShowDoNotDisturb()
            && <ListGroupItemBtn handler={this.onDoNotDisturb}>
              {SettingsView.renderSetting(
                'clock',
                'Do not disturb',
                <span>{this.props.doNotDisturbEnabled ? 'On' : 'Off'} <FAChevronRight /></span>,
              )}
            </ListGroupItemBtn>
            }
            <SwitchListGroupItem
              id="copyNotificationsEmail"
              value=""
              icon="envelope"
              description="Copy my alerts to email"
              role="button"
              tabIndex={0}
              onClick={this.onNotificationEmailCopyChange}
              checked={this.state.emailNotificationsOptIn.wantsEmails}
              failure={this.state.emailNotificationsOptIn.failed && !this.props.isOnline}
              loading={!this.state.emailNotificationsOptIn.fetchedOnce
                && this.props.emailNotificationsOptIn.fetching}
              disabled={!this.props.isOnline}
            />
            <ListGroupItemBtn handler={this.onSMS}>
              {SettingsView.renderSetting(
                'mobile-alt',
                'Copy my alerts to SMS',
                SettingsView.renderFetchedBool({
                  ...this.props.smsNotifications,
                }),
              )}
            </ListGroupItemBtn>
          </div>

          <div className="list-group setting-colour-1">
            {
              this.props.features.news ? <ListGroupItemBtn
                handler={ this.props.newsCategories.fetched
                  && !this.props.newsCategories.failed ? () => this.props.dispatch(
                    push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NEWS_CATEGORIES}`),
                  ) : null
                }
              >
                { SettingsView.renderSetting(
                  'newspaper',
                  'News categories',
                  SettingsView.renderFetchedCount({
                    ...this.props.newsCategories,
                  }),
                ) }
              </ListGroupItemBtn> : null}
            <ListGroupItemBtn handler={this.onLocationPreferences}>
              {SettingsView.renderSetting(
                'map-signs',
                'Location preferences',
                SettingsView.renderFetchedCount({
                  fetching: this.props.newsOptIn.fetching,
                  failed: this.props.newsOptIn.failed,
                  fetched: this.props.newsOptIn.fetched,
                  selected: this.props.newsOptIn.location.selected,
                  total: this.props.newsOptIn.location.total,
                  isOnline: this.props.isOnline,
                }),
              )}
            </ListGroupItemBtn>
          </div>

          {SettingsView.shouldShowTimetableAlarms()
          && <div className="list-group setting-colour-2">
            <ListGroupItemBtn handler={this.onTimetableAlarms}>
              {SettingsView.renderSetting(
                'bell',
                'Timetable alarms',
                <div>
                  {this.props.timetableAlarms.enabled
                    ? `${this.props.timetableAlarms.minutesBeforeEvent} minutes before`
                    : 'Off'}
                  <FAChevronRight />
                </div>,
              )}
            </ListGroupItemBtn>
          </div>
          }

          <div className="list-group setting-colour-2">
            <ListGroupItemBtn handler={this.onActivityFilter}>
              {SettingsView.renderSetting(
                'tachometer-alt',
                'Activity filter',
                SettingsView.renderFractionCount(
                  this.props.activityFilter.selected,
                  this.props.activityFilter.total,
                ),
              )}
            </ListGroupItemBtn>
            <ListGroupItemBtn handler={this.onNotificationFilter}>
              {SettingsView.renderSetting(
                'bell',
                'Alerts filter',
                SettingsView.renderFractionCount(
                  this.props.notificationFilter.selected,
                  this.props.notificationFilter.total,
                ),
              )}
            </ListGroupItemBtn>
          </div>

          <div className="list-group setting-colour-3">
            <ListGroupItemBtn handler={SettingsView.onWhatsNew}>
              {SettingsView.renderSetting(
                'question-circle',
                'What\'s new?',
                <FAChevronRight />,
              )}
            </ListGroupItemBtn>
            <ListGroupItemBtn handler={SettingsView.onSendFeedback}>
              <div className="media">
                <div className="media-left feedback">
                  <span className="fa-stack">
                    <i className="fal fa-fw fa-comment fa-stack-2x" />
                    <strong className="fa-fw fa-stack-1x">!</strong>
                  </span>
                </div>
                <div className="media-body">
                  Send feedback
                </div>
                <div className="media-right">
                  <FAChevronRight />
                </div>
              </div>
            </ListGroupItemBtn>
            {SettingsView.canLaunchTour()
            && <ListGroupItemBtn handler={SettingsView.launchTour}>
              {SettingsView.renderSetting(
                'arrow-alt-circle-right',
                'Take a tour',
                <FAChevronRight />,
              )}
            </ListGroupItemBtn>
            }
            <div className="list-group-item">
              {SettingsView.renderSetting('info-circle', `Version: ${this.getVersionString()}`, null)}
            </div>
          </div>

          <div className="list-group setting-colour-3">
            <ListGroupItemBtn handler={SettingsView.onSignOut}>
              <div className="media">
                <div className="media-left signout">
                  <i className="fal fa-fw fa-sign-out" />
                </div>
                <div className="media-body">
                  Sign out
                </div>
                <div className="media-right">
                  <FAChevronRight />
                </div>
              </div>
            </ListGroupItemBtn>
          </div>
        </div>
      </ScrollRestore>
    );
  }
}

// Remove from the user's filter choice any keys that are not present in the filter options
function ensureValidFilterChoice(filter, filterOptions) {
  return _.mapValues(filter, (options, optionType) => _.pickBy(
    options,
    (v, option) => filterOptions[optionType] !== undefined
      && _.find(
        filterOptions[optionType],
        filterOption => filterOption.id === option,
      ) !== undefined,
  ));
}

const select = (state) => {
  const activityFilterTotal = _.reduce(
    state.activities.filterOptions,
    (total, o) => total + o.length,
    0,
  );
  const notificationFilterTotal = _.reduce(
    state.notifications.filterOptions,
    (total, o) => total + o.length,
    0,
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
    smsNotifications: {
      enabled: state.smsNotifications.wantsSms,
      smsNumber: state.smsNotifications.smsNumber,
      fetching: state.smsNotifications.fetching,
      fetched: state.smsNotifications.fetched,
      failed: state.smsNotifications.failed,
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
    colourSchemes: {
      fetching: state.colourSchemes.fetching,
      failed: state.colourSchemes.failed,
      fetched: state.colourSchemes.fetched,
      chosen: state.colourSchemes.chosen,
      schemes: state.colourSchemes.schemes,
    },
    activityFilter: {
      selected: activityFilterTotal - _.reduce(
        ensureValidFilterChoice(state.activities.filter, state.activities.filterOptions),
        (total, o) => total + _.filter(o, v => !v).length,
        0,
      ),
      total: activityFilterTotal,
    },
    notificationFilter: {
      selected: notificationFilterTotal - _.reduce(
        ensureValidFilterChoice(state.notifications.filter, state.notifications.filterOptions),
        (total, o) => total + _.filter(o, v => !v).length,
        0,
      ),
      total: notificationFilterTotal,
    },
    assetsRevision: state.app.assets.revision,
    appVersion: state.app.native.version,
    isOnline: state.device.isOnline,
    doNotDisturbEnabled: state.doNotDisturb.enabled,
    timetableAlarms: state.timetableAlarms,
    eap: { ...state.eap },
    features: state.user.features,
  };
};

export default connect(select)(SettingsView);
