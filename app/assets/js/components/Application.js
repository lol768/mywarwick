import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import ID7Layout from './ui/ID7Layout';

import { connect } from 'react-redux';
import { push } from 'react-router-redux';
import log from 'loglevel';

import { getNumItemsSince } from '../stream';

export class Application extends ReactComponent {

  constructor(props) {
    super();
    this.onSelectItem = props.onSelectItem.bind(this);
  }

  render() {
    const { location, notificationsCount, layoutClassName, user, children }
      = this.props;

    log.debug('Application.render');

    return (
      <ID7Layout path={ location.pathname }>
        { children }
        { layoutClassName === 'mobile' ?
          <TabBar selectedItem={ location.pathname } onSelectItem={ this.onSelectItem }>
            <TabBarItem title="Me" icon="user" path="/" />
            <TabBarItem
              title="Notifications" icon="inbox" path="/notifications"
              badge={ notificationsCount } isDisabled = { !user.authenticated }
            />
            <TabBarItem
              title="Activity" icon="dashboard" path="/activity"
              isDisabled = { !user.authenticated }
            />
            <TabBarItem title="News" icon="mortar-board" path="/news" />
            <TabBarItem title="Search" icon="search" path="/search" />
          </TabBar>
          : null}
      </ID7Layout>
    );
  }

}

function mapStateToProps(state) {
  return {
    notificationsCount:
      getNumItemsSince(state.get('notifications'), state.getIn(['notificationsLastRead', 'date'])),
    layoutClassName: state.get('ui').get('className'),
    user: state.getIn(['user', 'data']).toJS(),
  };
}

// do all dispatching in here rather than referencing props.dispatch from the component,
// so that the plain component doesn't depend on redux.
function mapDispatchToProps(dispatch) {
  return {
    onSelectItem: (p) => {
      dispatch(push(p));
    },
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Application);
