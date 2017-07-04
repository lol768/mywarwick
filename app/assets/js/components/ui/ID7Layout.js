import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import _ from 'lodash-es';
import log from 'loglevel';
// import Badge from './Badge';
// import MastheadSearch from './MastheadSearch';
import MastheadMobile from './MastheadMobile';
import PermissionRequest from './PermissionRequest';
import MasqueradeNotice from './MasqueradeNotice';
import UpdatePopup from './UpdatePopup';
// import UtilityBar from './UtilityBar';
import { connect } from 'react-redux';
import { isEmbedded } from '../../embedHelper';
import { getNumItemsSince } from '../../stream';
import * as ui from '../../state/ui';
import { goBack, push } from 'react-router-redux';
import { Routes } from '../AppRoot';

class ID7Layout extends React.PureComponent {

  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    colourTheme: PropTypes.string.isRequired,
    user: PropTypes.shape({
      data: PropTypes.shape({
        authenticated: PropTypes.bool.isRequired,
      }).isRequired,
    }).isRequired,
    native: PropTypes.bool,
    showBetaWarning: PropTypes.bool,
    path: PropTypes.string.isRequired,
    zoomedTile: PropTypes.string,
    notificationsCount: PropTypes.number,
    children: PropTypes.node,
    layoutClassName: PropTypes.oneOf(['desktop', 'mobile']),
  };

  constructor(props) {
    super(props);
    this.onBackClick = this.onBackClick.bind(this);
    this.onEdit = this.onEdit.bind(this);
    this.onSettings = this.onSettings.bind(this);
  }

  componentWillMount() {
    this.props.dispatch(ui.updateUIContext());
    this.setBodyTheme(this.props.colourTheme);
  }

  componentDidMount() {
    this.updateHeaderHeight();
    if (isEmbedded()) {
      const type = 'message.id7.account-popover.layoutDidMount';
      window.parent.postMessage(JSON.stringify({ type }), '*');
    }
  }

  componentWillReceiveProps(nextProps) {
    nextProps.dispatch(ui.updateUIContext());

    const $body = $('body').removeClass((i, className) =>
      _.filter(className.split(' '), (singleClass) => _.startsWith(singleClass, 'in-')).join(' ')
    );
    const pathClasses = _.filter(nextProps.path.split('/'), (path) => path.length > 0);
    if (pathClasses.length === 0) {
      $body.addClass('in-root');
    } else {
      $body.addClass(_.map(pathClasses, (path) => `in-${path}`).join(' '));
    }
  }

  componentDidUpdate(prevProps) {
    this.updateHeaderHeight();
    if (prevProps.colourTheme !== this.props.colourTheme) {
      this.setBodyTheme(this.props.colourTheme, prevProps.colourTheme);
    }
  }

  onBackClick() {
    this.props.dispatch(goBack());
  }

  onEdit() {
    if (this.isEditing()) {
      this.props.dispatch(goBack());
    } else {
      this.props.dispatch(push(`/${Routes.EDIT}`));
    }
  }

  onSettings() {
    this.props.dispatch(push(`/${Routes.SETTINGS}`));
  }

  /** Set the theme on the html element, so that we can style everything. */
  setBodyTheme(newTheme, oldTheme = '') {
    $('html')
      .removeClass(`theme-${oldTheme}`)
      .addClass(`theme-${newTheme}`);
  }

  updateHeaderHeight() {
    const headerHeight = $(ReactDOM.findDOMNode(this.refs.header)).height();
    $(document.body).css('margin-top', headerHeight);
  }

  isEditing() {
    return this.props.path === `/${Routes.EDIT}`;
  }

  renderNotificationPermissionRequest() {
    if ('Notification' in window && Notification.permission === 'default' && !isEmbedded()) {
      return <PermissionRequest isDisabled={ !this.props.user.data.authenticated } />;
    }

    return null;
  }

  renderBetaWarning() {
    if (!this.props.native && this.props.showBetaWarning) {
      return (
        <div className="top-page-notice">
          My&nbsp;Warwick is currently being piloted and is not yet available for general use.
          Please visit our <a href="http://warwick.ac.uk/webteam/mywarwick/">My&nbsp;Warwick support pages</a> for more information.
        </div>
      );
    }
    return null;
  }

  renderMasqueradeNotice() {
    const user = this.props.user.data;

    if (user.masquerading) {
      return <MasqueradeNotice masqueradingAs={user} />;
    }

    return null;
  }

  renderMobile() {
    const { user, path } = this.props;

    const showSettingsButton = !(
      path.startsWith(`/${Routes.SETTINGS}`) ||
      path.startsWith(`/${Routes.POST_TOUR}`)
    );

    return (
      <div className="">
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>
        <div className="fixed-header at-top">
          <div>
            <header className="id7-page-header" ref="header">
              { this.renderBetaWarning() }
              { this.renderMasqueradeNotice() }

              <MastheadMobile user={user}
                onBackClick={this.onBackClick}
                path={path}
                onEdit={this.onEdit}
                editing={this.isEditing()}
                showEditButton={
                  this.isEditing() ||
                  path === '/'
                }
                onSettings={this.onSettings}
                showSettingsButton={showSettingsButton}
              />
            </header>
          </div>
        </div>

        <div>
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

  /** SWOO EDIT
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
   **/

  render() {
    // SWOO EDIT if (this.props.layoutClassName === 'mobile') {
    log.debug('ID7Layout.render:mobile');
    return this.renderMobile();
    // SWOO EDIT }

    /** SWOO EDIT if (!this.props.layoutClassName) {
      log.warn('props.layoutClassName not set');
    }
     */

    // SWOO EDIT log.debug('ID7Layout.render:desktop');
    // SWOO EDIT return this.renderDesktop();
  }
}

const select = (state) => ({
  layoutClassName: state.ui.className,
  notificationsCount:
    getNumItemsSince(state.notifications.stream, _.get(state, ['notificationsLastRead', 'date'])),
  user: state.user,
  colourTheme: state.ui.colourTheme,
  zoomedTile: state.ui.zoomedTile,
  native: state.ui.native,
  showBetaWarning: state.ui.showBetaWarning,
});

export default connect(select)(ID7Layout);
