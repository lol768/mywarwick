import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import _ from 'lodash';
import MastheadIcon from './MastheadIcon';
import NotificationsView from '../views/NotificationsView';
import ActivityView from '../views/ActivityView';
import LinkBlock from './LinkBlock';
import Link from './Link';
import NewsView from '../views/NewsView';
import MastheadSearch from './MastheadSearch';
import MastheadMobile from './MastheadMobile';
import PermissionRequest from './PermissionRequest';
import MasqueradeNotice from './MasqueradeNotice';
import UpdatePopup from './UpdatePopup';
import UtilityBar from './UtilityBar';
import { connect } from 'react-redux';
import { getNumItemsSince } from '../../stream';
import { updateUIContext } from '../Application';
import { fetchTileContent } from '../../serverpipe';
import { push, goBack } from 'react-router-redux';

class ID7Layout extends ReactComponent {

  constructor(props) {
    super(props);
    this.goToHome = this.goToHome.bind(this);
    this.goToNotification = this.goToNotification.bind(this);
    this.goToActivity = this.goToActivity.bind(this);
    this.onBackClick = this.onBackClick.bind(this);
  }

  componentWillMount() {
    this.props.dispatch(updateUIContext());
    this.setBodyTheme(this.props.colourTheme);
  }

  componentWillReceiveProps(nextProps) {
    nextProps.dispatch(updateUIContext());

    const hasZoomedTile = _(nextProps.path).startsWith('/tiles/');
    $('body').toggleClass('has-zoomed-tile', hasZoomedTile);
  }

  componentDidUpdate(prevProps) {
    const headerHeight = $(ReactDOM.findDOMNode(this.refs.header)).height();
    $('.id7-main-content-area').css('padding-top', headerHeight);
    if (prevProps.colourTheme !== this.props.colourTheme) {
      this.setBodyTheme(this.props.colourTheme, prevProps.colourTheme);
    }
  }

  /** Set the theme on the body element, so that we can style everything. */
  setBodyTheme(newTheme, oldTheme = '') {
    $(document.body)
      .removeClass(`theme-${oldTheme}`)
      .addClass(`theme-${newTheme}`);
  }

  goToHome(e) {
    e.preventDefault();
    this.props.dispatch(push('/'));
    this.props.dispatch(fetchTileContent());
  }

  goToNotification() {
    this.props.dispatch(push('/notifications'));
  }

  goToActivity() {
    this.props.dispatch(push('/activity'));
  }

  onBackClick() {
    this.props.dispatch(goBack());
  }

  renderMasqueradeNotice() {
    const user = this.props.user.data;

    if (user.masquerading) {
      return <MasqueradeNotice masqueradingAs={user}/>;
    }
  }

  renderNotificationPermissionRequest() {
    if ('Notification' in window && Notification.permission === 'default') {
      return <PermissionRequest isDisabled={ !this.props.user.data.authenticated }/>;
    }
  }

  renderMobile() {
    return (
      <div>
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>
        <div className="fixed-header at-top">
          <div className="id7-fixed-width-container">
            <header className="id7-page-header" ref="header">
              { this.renderMasqueradeNotice() }

              <MastheadMobile user={this.props.user}
                zoomedTile={this.props.zoomedTile}
                onBackClick={this.onBackClick}
                path={this.props.path}
              />
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
              { this.renderMasqueradeNotice() }
              <div className="id7-utility-masthead">
                <nav className="id7-utility-bar" id="utility-bar-container">
                  <UtilityBar user={this.props.user} layoutClassName="desktop"/>
                </nav>
                <div className="id7-masthead">
                  <div className="id7-masthead-contents">
                    <div className="clearfix">
                      <div className="id7-logo-column">
                        <div className="id7-logo-row">
                          <div className="id7-logo">
                            <a href="/" title="Warwick homepage" onClick={ this.goToHome }>
                              <img src="" alt="Warwick"/>
                            </a>
                          </div>
                          <div className="masthead-popover-icons">
                            <MastheadIcon
                              icon="inbox"
                              badge={ notificationsCount }
                              key="notifications"
                              popoverTitle="Notifications"
                              isDisabled={ !user.data.authenticated }
                              onMore={ this.goToNotification }
                            >
                              <NotificationsView grouped={false}/>
                            </MastheadIcon>
                            <MastheadIcon
                              icon="dashboard" key="activity"
                              popoverTitle="Activity"
                              isDisabled={ !user.data.authenticated }
                              onMore={ this.goToActivity }
                            >
                              <ActivityView grouped={false}/>
                            </MastheadIcon>
                            <MastheadIcon icon="bars" key="links" popoverTitle="Quick links">
                              <LinkBlock columns="1">
                                <Link key="bpm" href="http://warwick.ac.uk/bpm">
                                  Course Transfers
                                </Link>
                                <Link key="ett" href="http://warwick.ac.uk/ett">
                                  Exam Timetable
                                </Link>
                                <Link key="massmail" href="http://warwick.ac.uk/massmail">
                                  Mass Mailing
                                </Link>
                                <Link key="mrm" href="http://warwick.ac.uk/mrm">
                                  Module Registration
                                </Link>
                                <Link
                                  key="printercredits"
                                  href="http://warwick.ac.uk/printercredits"
                                >
                                  Printer Credits
                                </Link>
                              </LinkBlock>
                            </MastheadIcon>
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
              <div className="row">
                <div className="col-sm-8 col-lg-9">
                  {this.props.children}
                </div>
                <div className="col-sm-4 col-lg-3">
                  <NewsView />
                </div>
              </div>
            </div>
          </main>
        </div>
        <div className="id7-right-border"></div>
      </div>
    );
  }

  render() {
    if (this.props.layoutClassName === 'mobile') {
      return this.renderMobile();
    }

    return this.renderDesktop();
  }
}

const select = (state) => ({
  layoutClassName: state.get('ui').get('className'),
  notificationsCount:
    getNumItemsSince(state.get('notifications'), state.getIn(['notificationsLastRead', 'date'])),
  user: state.get('user').toJS(),
  colourTheme: state.get('ui').get('colourTheme'),
  zoomedTile: state.getIn(['me', 'zoomedTile']),
});

export default connect(select)(ID7Layout);
