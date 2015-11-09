import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';

import UtilityBar from './UtilityBar';

import { MastheadPopoverIcons, MastheadIcon } from './MastheadPopoverIcons';
import NotificationsView from '../views/NotificationsView';
import ActivityView from '../views/ActivityView';
import ID7SearchColumn from './ID7SearchColumn';

import { connect } from 'react-redux';
import { getStreamSize } from '../../stream';

class ID7Layout extends ReactComponent {

  render() {
    let isDesktop = this.props.layoutClassName == 'desktop';

    return (
      <div>
        <a className="sr-only sr-only-focusable" href="#main">Skip to main content</a>

        <header className="id7-page-header">
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
                        <a href="http://warwick.ac.uk" title="Warwick homepage">
                          <img src="" alt="Warwick"/>
                        </a>
                      </div>
                      { isDesktop ?
                        <MastheadPopoverIcons>
                          <MastheadIcon icon="globe" badge={this.props.notificationsCount} key="notifications">
                            <NotificationsView />
                          </MastheadIcon>
                          <MastheadIcon icon="dashboard" key="activity">
                            <ActivityView />
                          </MastheadIcon>
                        </MastheadPopoverIcons>
                        : null }
                    </div>
                  </div>
                  { isDesktop ? <ID7SearchColumn /> : null }
                </div>
              </div>
            </div>
          </div>
        </header>

        <main className="id7-main-content-area" id="main">
          <header className="id7-main-content-header">
            <div className="id7-horizontal-divider">
              <svg xmlns="http://www.w3.org/2000/svg" x="0" y="0" version="1.1" width="1130" height="40"
                   viewBox="0, 0, 1130, 40">
                <path d="m 0,0 1030.48, 0 22.8,40 16.96,-31.4 16.96,31.4 22.8,-40 20,0" className="divider"
                      stroke="#383838" fill="none"/>
              </svg>
            </div>
          </header>

          <div className="id7-main-content">
            {this.props.children}
          </div>
        </main>
      </div>
    );
  }

}

let select = (state) => {
  return {
    layoutClassName: state.get('ui').get('className'),
    notificationsCount: getStreamSize(state.get('notifications'))
  };
};

export default connect(select)(ID7Layout);