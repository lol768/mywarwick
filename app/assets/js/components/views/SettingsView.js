import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import { connect } from 'react-redux';
import { push } from 'react-router-redux';
import { Routes } from '../AppRoot';
import HideableView from './HideableView';
import * as newsCategories from '../../state/news-categories';
import * as newsOptIn from '../../state/news-optin';
import { loadDeviceDetails } from '../../userinfo';
import _ from 'lodash-es';

class SettingsView extends HideableView {

  static propTypes = {
    mutes: PropTypes.number.isRequired,
    newsCategories: PropTypes.shape({
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
      selected: PropTypes.number.isRequired,
      total: PropTypes.number.isRequired,
    }).isRequired,
    newsOptIn: PropTypes.shape({
      fetching: PropTypes.bool.isRequired,
      failed: PropTypes.bool.isRequired,
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
    const { fetching, failed, selected, total } = props;
    if (fetching) {
      return (
        <div>
          <i className="fa fa-spinner fa-pulse" />
          <i className="fa fa-fw fa-chevron-right" />
        </div>
      );
    } else if (failed) {
      return (
        <div>
          <i className="fa fa-explamation-circle text-danger" />
          <i className="fa fa-fw fa-chevron-right" />
        </div>
      );
    }
    return SettingsView.renderFractionCount(selected, total);
  }

  componentDidShow() {
    this.props.dispatch(newsCategories.fetch());
    this.props.dispatch(newsOptIn.fetch());
  }

  render() {
    return (
      <div>
        <div className="list-group">
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
        </div>

        <div className="list-group setting-colour-1">
          <div className="list-group-item"
            onClick={ () =>
              this.props.dispatch(
                push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.NEWS_CATEGORIES}`)
              )
            }
          >
            { SettingsView.renderSetting(
              'newspaper-o',
              'News categories',
              SettingsView.renderFetchedCount(this.props.newsCategories)
            ) }
          </div>
          <div className="list-group-item"
            onClick={ () =>
              this.props.dispatch(
                push(
                  `/${Routes.SETTINGS}/${Routes.SettingsRoutes.OPT_IN}/` +
                  `${Routes.SettingsRoutes.OptInTypes.LOCATION}`
                )
              )
            }
          >
            { SettingsView.renderSetting(
              'map-signs',
              'Location preferences',
              SettingsView.renderFetchedCount({
                fetching: this.props.newsOptIn.fetching,
                failed: this.props.newsOptIn.failed,
                selected: this.props.newsOptIn.location.selected,
                total: this.props.newsOptIn.location.total,
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
        </div>
      </div>
    );
  }
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
    newsCategories: {
      fetching: state.newsCategories.fetching,
      failed: state.newsCategories.failed,
      selected: state.newsCategories.subscribed.length,
      total: state.newsCategories.items.length,
    },
    newsOptIn: {
      fetching: state.newsOptIn.fetching,
      failed: state.newsOptIn.failed,
      location: {
        selected: (state.newsOptIn.selected.Location || []).length,
        total: (state.newsOptIn.options.Location || []).length,
      },
    },
    activityFilter: {
      selected: activityFilterTotal - _.reduce(
        state.activities.filter,
        (total, o) => total + _.filter(o, v => !v).length,
        0
      ),
      total: activityFilterTotal,
    },
    notificationFilter: {
      selected: notificationFilterTotal - _.reduce(
        state.notifications.filter,
        (total, o) => total + _.filter(o, v => !v).length,
        0
      ),
      total: notificationFilterTotal,
    },
  };
};

export default connect(select)(SettingsView);
