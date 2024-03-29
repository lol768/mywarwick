/* eslint-env browser */

import React from 'react';
import * as PropTypes from 'prop-types';
import $ from 'jquery';
import _ from 'lodash-es';
import log from 'loglevel';
import { goBack, push } from 'react-router-redux';
import { connect } from 'react-redux';
import MastheadMobile from './MastheadMobile';
import PermissionRequest from './PermissionRequest';
import MasqueradeNotice from './MasqueradeNotice';
import UpdatePopup from './UpdatePopup';
import isEmbedded from '../../embedHelper';
import { getNumItemsSince } from '../../stream';
import * as ui from '../../state/ui';
import { Routes } from '../AppRoot';
import wrapKeyboardSelect from '../../keyboard-nav';

class ID7Layout extends React.PureComponent {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    colourTheme: PropTypes.string.isRequired,
    schemeColour: PropTypes.string.isRequired,
    colourSchemesLoaded: PropTypes.bool.isRequired,
    user: PropTypes.shape({
      data: PropTypes.shape({
        authenticated: PropTypes.bool.isRequired,
      }).isRequired,
    }).isRequired,
    path: PropTypes.string.isRequired,
    zoomedTile: PropTypes.string,
    notificationsCount: PropTypes.number,
    children: PropTypes.node,
    features: PropTypes.object.isRequired,
  };

  /** Set the theme on the html element, so that we can style everything. */
  static setColourTheme(newProps) {
    if (newProps.colourSchemesLoaded) {
      $('html')
        .removeClass((i, className) =>
          _.filter(className.split(' '), singleClass => _.startsWith(singleClass, 'theme-')).join(' '),
        )
        .addClass(`theme-${newProps.colourTheme}`)
        .find('meta[name="theme-color"]')
        .attr('content', newProps.schemeColour);

      if (isEmbedded()) {
        const data = {
          type: 'message.id7.account-popover.colourThemeChange',
          colourTheme: newProps.colourTheme,
        };
        window.parent.postMessage(JSON.stringify(data), '*');
      }
    }
  }

  constructor(props) {
    super(props);
    this.onBackClick = this.onBackClick.bind(this);
    this.onEditComplete = this.onEditComplete.bind(this);
    this.onSettings = this.onSettings.bind(this);
    this.dismissBetaWarning = this.dismissBetaWarning.bind(this);

    this.state = { betaWarningDismissed: false };
  }

  componentWillMount() {
    this.props.dispatch(ui.updateUIContext());
    ID7Layout.setColourTheme(this.props);
  }

  componentDidMount() {
    if (isEmbedded()) {
      const data = {
        type: 'message.id7.account-popover.layoutDidMount',
        colourTheme: this.props.colourTheme,
      };
      window.parent.postMessage(JSON.stringify(data), '*');
    }
  }

  componentWillReceiveProps(nextProps) {
    nextProps.dispatch(ui.updateUIContext());

    const $body = $('body').removeClass((i, className) =>
      _.filter(className.split(' '), singleClass => _.startsWith(singleClass, 'in-')).join(' '),
    );
    const pathClasses = _.filter(nextProps.path.split('/'), path => path.length > 0);
    if (pathClasses.length === 0) {
      $body.addClass('in-root');
    } else {
      $body.addClass(_.map(pathClasses, path => `in-${path}`).join(' '));
    }
  }

  componentDidUpdate(prevProps) {
    if (
      prevProps.colourTheme !== this.props.colourTheme ||
        prevProps.colourSchemesLoaded !== this.props.colourSchemesLoaded
    ) {
      ID7Layout.setColourTheme(this.props);
    }
  }

  onBackClick(e) {
    wrapKeyboardSelect(() => this.props.dispatch(goBack()), e);
  }

  onEditComplete(e) {
    wrapKeyboardSelect(() => {
      if (this.isEditing()) {
        this.props.dispatch(goBack());
      } else if (!this.props.features.updateTileEditUI) {
        this.props.dispatch(push(`/${Routes.EDIT}`));
      }
    }, e);
  }

  onSettings(e) {
    wrapKeyboardSelect(() => this.props.dispatch(push(`/${Routes.SETTINGS}`)), e);
  }

  isEditing() {
    return this.props.path === `/${Routes.EDIT}` && !this.props.features.updateTileEditUI;
  }

  dismissBetaWarning() {
    this.setState({ betaWarningDismissed: true });
  }

  renderNotificationPermissionRequest() {
    if ('Notification' in window && Notification.permission === 'default' && !isEmbedded()) {
      return <PermissionRequest isDisabled={ !this.props.user.data.authenticated } />;
    }

    return null;
  }

  renderMasqueradeNotice() {
    const user = this.props.user.data;
    const $body = $('body');

    if (user.masquerading) {
      $body.addClass('masquerading');
      return <MasqueradeNotice masqueradingAs={user} />;
    }

    $body.removeClass('masquerading');
    return null;
  }

  renderMobile() {
    const { user, path, features } = this.props;

    const showSettingsButton = !(
      _.startsWith(path, `/${Routes.SETTINGS}`) ||
      _.startsWith(path, `/${Routes.POST_TOUR}`) ||
      (features.updateTileEditUI && _.startsWith(path, `/${Routes.EDIT}`))
    );

    return (
      <div className="">
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>
        <div className="fixed-header at-top">
          <div>
            <header className="id7-page-header" ref="header">
              { this.renderMasqueradeNotice() }

              <MastheadMobile
                user={user}
                onBackClick={this.onBackClick}
                path={path}
                onEditComplete={this.onEditComplete}
                editing={this.isEditing()}
                showEditButton={
                  this.isEditing() ||
                  (path === '/' && !this.props.features.updateTileEditUI)
                }
                onSettings={this.onSettings}
                showSettingsButton={showSettingsButton}
                features={features}
              />
            </header>
          </div>
        </div>
        <div className="outer-container">
          <main className="id7-main-content-area" id="main">
            <header className="id7-main-content-header">
              { this.renderNotificationPermissionRequest() }
              <UpdatePopup />
            </header>

            <div className="main-content">
              { this.props.children }
            </div>
          </main>
        </div>
      </div>
    );
  }

  render() {
    log.debug('ID7Layout.render:mobile');
    return this.renderMobile();
  }
}

const select = state => ({
  notificationsCount:
    getNumItemsSince(state.notifications.stream, _.get(state, ['notificationsLastRead', 'date'])),
  user: state.user,
  colourTheme: state.ui.colourTheme,
  schemeColour: state.ui.schemeColour,
  colourSchemesLoaded: state.colourSchemes.loaded,
  zoomedTile: state.ui.zoomedTile,
  features: state.user.features,
});

export default connect(select)(ID7Layout);
