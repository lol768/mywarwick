import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';

import $ from 'jquery';

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

import { navigate } from '../../navigate';

import { connect } from 'react-redux';
import { getNumItemsSince } from '../../stream';

import { updateLayoutClass } from '../Application';
import { fetchTileContent } from '../../serverpipe';

class ID7Layout extends ReactComponent {

  constructor(props) {
    super(props);
    this.goToHome = this.goToHome.bind(this);
    this.goToNotification = this.goToNotification.bind(this);
    this.goToActivity = this.goToActivity.bind(this);
  }

  componentWillMount() {
    this.props.dispatch(updateLayoutClass());
    this.setBodyTheme(this.props.colourTheme);
  }

  componentWillReceiveProps() {
    this.props.dispatch(updateLayoutClass());
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
    this.props.dispatch(navigate('/'));
    this.props.dispatch(fetchTileContent());
  }

  goToNotification() {
    this.props.dispatch(navigate('/notifications'));
  }

  goToActivity() {
    this.props.dispatch(navigate('/activity'));
  }

  render() {
    const { layoutClassName, notificationsCount, user }
      = this.props;

    const isMobile = layoutClassName === 'mobile';
    const userData = this.props.user.data;


    return (
      <div>
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>

        <div className="fixed-header at-top">
          <div className="id7-fixed-width-container">
            <header className="id7-page-header" ref="header">
              { userData.masquerading ?
                <MasqueradeNotice masqueradingAs={userData}/> : null
              }
              { !isMobile ?
                <div className="id7-utility-masthead">
                  <nav className="id7-utility-bar" id="utility-bar-container">
                    <UtilityBar user={this.props.user}
                      layoutClassName={this.props.layoutClassName}
                    />
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
                                isDisabled={ !user.authenticated }
                                onMore={ this.goToNotification }
                              >
                                <NotificationsView grouped={false}/>
                              </MastheadIcon>
                              <MastheadIcon
                                icon="dashboard" key="activity"
                                popoverTitle="Activity"
                                isDisabled={ !user.authenticated }
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
                :
              <MastheadMobile user={this.props.user}
                layoutClassName={this.props.layoutClassName}
              />}
            </header>
          </div>
        </div>

        <div className="id7-fixed-width-container">

          <main className="id7-main-content-area" id="main">
            <header className="id7-main-content-header">
              { 'Notification' in window && Notification.permission === 'default' ?
                <PermissionRequest isDisabled={ !user.authenticated }/> : null }
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
              { !isMobile ?
                <div className="row">
                  <div className="col-sm-8 col-lg-9">
                    {this.props.children}
                  </div>
                  <div className="col-sm-4 col-lg-3">
                    <NewsView />
                  </div>
                </div>
                : this.props.children }
            </div>
          </main>
        </div>
      </div>
    );
  }
}

const select = (state) => { // eslint-disable-line arrow-body-style
  return {
    layoutClassName: state.get('ui').get('className'),
    notificationsCount:
      getNumItemsSince(state.get('notifications'), state.get('notifications-lastRead')),
    user: state.get('user').toJS(),
    colourTheme: state.get('ui').get('colourTheme'),
  };
};

export default connect(select)(ID7Layout);
