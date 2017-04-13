import React, { PropTypes } from 'react';
import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import ID7Layout from './ui/ID7Layout';
import { connect } from 'react-redux';
import _ from 'lodash-es';
import log from 'loglevel';
import { getNumItemsSince } from '../stream';
import { Routes } from './AppRoot';
import { navRequest } from '../state/ui';

export class AppLayout extends React.Component {

  static propTypes = {
    onSelectItem: PropTypes.func.isRequired,
    location: PropTypes.shape({
      pathname: PropTypes.string.isRequired,
    }).isRequired,
    user: PropTypes.shape({
      authenticated: PropTypes.bool.isRequired,
    }).isRequired,
    notificationsCount: PropTypes.number.isRequired,
    layoutClassName: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired,
  };

  constructor(props) {
    super();
    this.onSelectItem = props.onSelectItem.bind(this);
  }

  render() {
    const { location, notificationsCount, layoutClassName, children, user }
      = this.props;

    log.debug('AppLayout.render');

    return (
      <div>
        <ID7Layout path={ location.pathname }>
          { children }
        </ID7Layout>
        { layoutClassName === 'mobile' ?
          <TabBar selectedItem={ location.pathname } onSelectItem={ this.onSelectItem }>
            <TabBarItem title="Me" icon="user" path="/" />
            <TabBarItem
              title="Notifications" icon="inbox" path={ `/${Routes.NOTIFICATIONS}` }
              badge={ notificationsCount } isDisabled={ !user.authenticated }
            />
            <TabBarItem
              title="Activity" icon="dashboard" path={ `/${Routes.ACTIVITY}` }
              isDisabled={ !user.authenticated }
            />
            <TabBarItem title="News" icon="mortar-board" path={ `/${Routes.NEWS}` } />
            <TabBarItem title="Search" icon="search" path={ `/${Routes.SEARCH}` } />
          </TabBar>
          : null }
      </div>
    );
  }

}

function mapStateToProps(state) {
  return {
    notificationsCount:
      getNumItemsSince(state.notifications.stream, _.get(state, ['notificationsLastRead', 'date'])),
    layoutClassName: state.ui.className,
    user: state.user.data,
  };
}

// do all dispatching in here rather than referencing props.dispatch from the component,
// so that the plain component doesn't depend on redux.
function mapDispatchToProps(dispatch) {
  return {
    onSelectItem: (p) => navRequest(p, dispatch),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(AppLayout);
