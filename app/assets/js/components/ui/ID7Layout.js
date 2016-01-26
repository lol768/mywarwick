import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';

import $ from 'jquery';
import UtilityBar from './UtilityBar';

import MastheadIcon from './MastheadIcon';
import NotificationsView from '../views/NotificationsView';
import ActivityView from '../views/ActivityView';
import { SettingsView } from '../views/SettingsView';
import LinkBlock from './LinkBlock';
import Link from './Link';
import NewsView from '../views/NewsView';
import MastheadSearch from './MastheadSearch';
import PermissionRequest from './PermissionRequest';
import MasqueradeNotice from './MasqueradeNotice';

import { navigate } from '../../navigate';

import { connect } from 'react-redux';
import { getStreamSize } from '../../stream';

import { updateLayoutClass } from '../Application';

class ID7Layout extends ReactComponent {

  componentWillMount() {
    this.props.dispatch(updateLayoutClass());
  }

  componentWillReceiveProps() {
    this.props.dispatch(updateLayoutClass());
  }

  componentDidUpdate() {
    var headerHeight = $(ReactDOM.findDOMNode(this.refs.header)).height();

    $(document.body).css('margin-top', headerHeight);
  }

  goToHome(e) {
    e.preventDefault();

    this.props.dispatch(navigate('/'));
  }

  render() {
    let isDesktop = this.props.layoutClassName == 'desktop';

    return (
      <div className={'theme-' + this.props.colourTheme}>
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>

        <div className="fixed-header at-top">
          <div className="id7-fixed-width-container">
            <header className="id7-page-header" ref="header">
              { this.props.user.masquerading ? <MasqueradeNotice masqueradingAs={this.props.user} /> : null}
              <div className="id7-utility-masthead">
                <nav className="id7-utility-bar" id="utility-bar-container">
                  {this.props.utilityBar}
                </nav>
                <div className="id7-masthead">

                  <div className="id7-masthead-contents">
                    <div className="clearfix">
                      <div className="id7-logo-column">
                        <div className="id7-logo-row">
                          <div className="id7-logo">
                            <a href="/" title="Warwick homepage" onClick={this.goToHome.bind(this)}>
                              <img src="" alt="Warwick"/>
                            </a>
                          </div>
                          { isDesktop ?
                            <div className="masthead-popover-icons">
                              <MastheadIcon icon="inbox"
                                            badge={this.props.notificationsCount}
                                            key="notifications"
                                            popoverTitle="Notifications"
                                            isDisabled = { !this.props.user.authenticated }
                                            onMore={() => this.props.dispatch(navigate('/notifications'))}>
                                <NotificationsView grouped={false}/>
                              </MastheadIcon>
                              <MastheadIcon icon="dashboard" key="activity" badge={this.props.activitiesCount}
                                            popoverTitle="Activity"
                                            isDisabled = { !this.props.user.authenticated }
                                            onMore={() => this.props.dispatch(navigate('/activity'))}>
                                <ActivityView grouped={false}/>
                              </MastheadIcon>
                              <MastheadIcon icon="bars" key="links" popoverTitle="Quick links">
                                <LinkBlock columns="1">
                                  <Link key="bpm" href="http://warwick.ac.uk/bpm">Course Transfers</Link>
                                  <Link key="ett" href="http://warwick.ac.uk/ett">Exam Timetable</Link>
                                  <Link key="massmail" href="http://warwick.ac.uk/massmail">Mass Mailing</Link>
                                  <Link key="mrm" href="http://warwick.ac.uk/mrm">Module Registration</Link>
                                  <Link key="printercredits" href="http://warwick.ac.uk/printercredits">Printer
                                    Credits</Link>
                                </LinkBlock>
                              </MastheadIcon>
                            </div>
                            : null }
                        </div>
                      </div>
                      { isDesktop ? <MastheadSearch /> : null }
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
              { Notification && Notification.permission === "default" ?
                <PermissionRequest isDisabled={ !this.props.user.authenticated } /> : null }
              <div className="id7-horizontal-divider">
                <svg xmlns="http://www.w3.org/2000/svg" x="0" y="0" version="1.1" width="1130" height="40"
                     viewBox="0, 0, 1130, 40">
                  <path d="m 0,0 1030.48, 0 22.8,40 16.96,-31.4 16.96,31.4 22.8,-40 20,0" className="divider"
                        stroke="#383838" fill="none"/>
                </svg>
              </div>
            </header>

            <div className="id7-main-content">
              { isDesktop ?
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

let select = (state) => {
  return {
    layoutClassName: state.get('ui').get('className'),
    notificationsCount: getStreamSize(state.get('notifications')),
    activitiesCount: getStreamSize(state.get('activities')),
    user: state.get('user').toJS(),
    colourTheme: state.get('ui').get('colourTheme')
  };
};

export default connect(select)(ID7Layout);