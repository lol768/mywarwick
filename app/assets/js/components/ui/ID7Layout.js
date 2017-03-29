import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import _ from 'lodash';
import log from 'loglevel';
import Badge from './Badge';
import MastheadSearch from './MastheadSearch';
import MastheadMobile from './MastheadMobile';
import PermissionRequest from './PermissionRequest';
import MasqueradeNotice from './MasqueradeNotice';
import UpdatePopup from './UpdatePopup';
import UtilityBar from './UtilityBar';
import { connect } from 'react-redux';
import { getNumItemsSince } from '../../stream';
import * as ui from '../../state/ui';
import { goBack } from 'react-router-redux';

class ID7Layout extends ReactComponent {

  constructor(props) {
    super(props);
    this.onBackClick = this.onBackClick.bind(this);
  }

  componentWillMount() {
    this.props.dispatch(ui.updateUIContext());
    this.setBodyTheme(this.props.colourTheme);
  }

  componentDidMount() {
    this.updateHeaderHeight();
  }

  componentWillReceiveProps(nextProps) {
    nextProps.dispatch(ui.updateUIContext());

    const hasZoomedTile = _(nextProps.path).startsWith('/tiles/');
    $('body').toggleClass('has-zoomed-tile', hasZoomedTile);
  }

  componentDidUpdate(prevProps) {
    this.updateHeaderHeight();
    if (prevProps.colourTheme !== this.props.colourTheme) {
      this.setBodyTheme(this.props.colourTheme, prevProps.colourTheme);
    }
  }

  updateHeaderHeight() {
    const headerHeight = $(ReactDOM.findDOMNode(this.refs.header)).height();
    $('.id7-main-content-area').css('top', headerHeight);
  }

  /** Set the theme on the body element, so that we can style everything. */
  setBodyTheme(newTheme, oldTheme = '') {
    $(document.body)
      .removeClass(`theme-${oldTheme}`)
      .addClass(`theme-${newTheme}`);
  }

  onBackClick() {
    this.props.dispatch(goBack());
  }

  renderMasqueradeNotice() {
    const user = this.props.user.data;

    if (user.masquerading) {
      return <MasqueradeNotice masqueradingAs={user} />;
    }

    return null;
  }

  renderNotificationPermissionRequest() {
    if ('Notification' in window && Notification.permission === 'default') {
      return <PermissionRequest isDisabled={ !this.props.user.data.authenticated } />;
    }

    return null;
  }

  renderBetaWarning() {
    if (!this.props.native) {
      return (
        <div className="top-page-notice">
          My&nbsp;Warwick is currently being piloted and is not yet available for general use.
          Please visit our <a href="http://warwick.ac.uk/webteam/mywarwick/">My&nbsp;Warwick support pages</a> for more information.
        </div>
      );
    }
    return null;
  }

  renderMobile() {
    const { user, zoomedTile, path } = this.props;
    return (
      <div className="">
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>
        <div className="fixed-header at-top">
          <div className="id7-fixed-width-container">
            <header className="id7-page-header" ref="header">
              { this.renderBetaWarning() }
              { this.renderMasqueradeNotice() }

              <MastheadMobile user={user}
                zoomedTile={zoomedTile}
                onBackClick={this.onBackClick}
                path={path}
              />
            </header>
          </div>
        </div>

        <div className="id7-fixed-width-container">
          <main className="id7-main-content-area" id="main" data-scrollable>
            <header className="id7-main-content-header">
              { this.renderNotificationPermissionRequest() }
              <UpdatePopup />
              <div className="id7-horizontal-divider">
                <svg
                  xmlns="http://www.w3.org/2000/svg" x="0" y="0" version="1.1" width="1130"
                  height="40" viewBox="0, 0, 1130, 40"
                >
                  <path
                    d="m 0,0 1030.48, 0 22.8,40 16.96,-31.4 16.96,31.4 22.8,-40 20,0"
                    className="divider" stroke="#383838" fill="none"
                  />
                </svg>
              </div>
            </header>

            <div className="id7-main-content">
              { this.props.children }
            </div>
          </main>
        </div>
      </div>
    );
  }

  renderDesktop() {
    const { notificationsCount, user } = this.props;

    return (
      <div>
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>
        <div className="id7-left-border"></div>
        <div className="fixed-header at-top">
          <div className="id7-fixed-width-container">
            <header className="id7-page-header" ref="header">
              { this.renderBetaWarning() }
              { this.renderMasqueradeNotice() }
              <div className="id7-utility-masthead">
                <nav className="id7-utility-bar" id="utility-bar-container">
                  <UtilityBar user={user} layoutClassName="desktop" />
                </nav>
                <div className="id7-masthead">
                  <div className="id7-masthead-contents">
                    <div className="clearfix">
                      <div className="id7-logo-column">
                        <div className="id7-logo-row">
                          <div className="id7-logo">
                            <a href="http://warwick.ac.uk" title="Warwick homepage">
                              <img src="" alt="Warwick" />
                            </a>
                            <Badge count={ notificationsCount } className="badge--red" />
                          </div>
                        </div>
                      </div>
                      <MastheadSearch />
                    </div>
                  </div>
                </div>
              </div>
            </header>
          </div>
        </div>

        <div className="id7-fixed-width-container">
          <main className="id7-main-content-area" id="main">
            <header className="id7-main-content-header">
              { this.renderNotificationPermissionRequest() }
              <UpdatePopup />
              <div className="id7-horizontal-divider">
                <svg
                  xmlns="http://www.w3.org/2000/svg" x="0" y="0" version="1.1" width="1130"
                  height="40" viewBox="0, 0, 1130, 40"
                >
                  <path
                    d="m 0,0 1030.48, 0 22.8,40 16.96,-31.4 16.96,31.4 22.8,-40 20,0"
                    className="divider" stroke="#383838" fill="none"
                  />
                </svg>
              </div>
            </header>

            <div className="id7-main-content">
              { this.props.children }
            </div>
          </main>
        </div>
        <div className="id7-right-border"></div>
      </div>
    );
  }

  render() {
    if (this.props.layoutClassName === 'mobile') {
      log.debug('ID7Layout.render:mobile');
      return this.renderMobile();
    }
    if (!this.props.layoutClassName) {
      log.warn('props.layoutClassName not set');
    }

    log.debug('ID7Layout.render:desktop');
    return this.renderDesktop();
  }
}

const select = (state) => ({
  layoutClassName: state.ui.className,
  notificationsCount:
    getNumItemsSince(state.notifications.stream, _(state).get(['notificationsLastRead', 'date'])),
  user: state.user,
  colourTheme: state.ui.colourTheme,
  zoomedTile: state.ui.zoomedTile,
  native: state.ui.native,
});

export default connect(select)(ID7Layout);
