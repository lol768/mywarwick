import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash-es';
import log from 'loglevel';
import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import ID7Layout from './ui/ID7Layout';
import { getNumItemsSince } from '../stream';
import { Routes } from './AppRoot';
import { navRequest } from '../state/ui';

export class AppLayout extends React.PureComponent {
  static propTypes = {
    onSelectItem: PropTypes.func.isRequired,
    location: PropTypes.shape({
      pathname: PropTypes.string.isRequired,
    }).isRequired,
    authenticated: PropTypes.bool.isRequired,
    notificationsCount: PropTypes.number.isRequired,
    children: PropTypes.node.isRequired,
    features: PropTypes.object,
  };

  constructor(props) {
    super();
    this.onSelectItem = props.onSelectItem.bind(this);
  }

  render() {
    const { location, notificationsCount, children, authenticated }
      = this.props;

    log.debug('AppLayout.render');

    return (
      <div>
        <ID7Layout path={ location.pathname }>
          { children }
        </ID7Layout>
        {
          <TabBar selectedItem={ location.pathname } onSelectItem={ this.onSelectItem }>
            <TabBarItem title="Me" icon="user-o" selectedIcon="user" path="/" />
            <TabBarItem
              title="Alerts"
              icon="bell-o"
              selectedIcon="bell"
              path={ `/${Routes.NOTIFICATIONS}` }
              badge={ notificationsCount }
              isDisabled={ !authenticated }
            />
            <TabBarItem
              title="Activity"
              icon="tachometer"
              selectedIcon="tachometer"
              path={ `/${Routes.ACTIVITY}` }
              isDisabled={ !authenticated }
            />
            {this.props.features.news ? <TabBarItem
              title="News"
              icon="newspaper-o"
              selectedIcon="newspaper-o"
              path={`/${Routes.NEWS}`}
            /> : null}
            <TabBarItem
              title="Search"
              icon="search"
              selectedIcon="search"
              path={ `/${Routes.SEARCH}` }
            />
          </TabBar>
        }
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    notificationsCount:
      getNumItemsSince(state.notifications.stream, _.get(state, ['notificationsLastRead', 'date'])),
    authenticated: state.user.data.authenticated,
    features: state.user.features,
  };
}

// do all dispatching in here rather than referencing props.dispatch from the component,
// so that the plain component doesn't depend on redux.
function mapDispatchToProps(dispatch) {
  return {
    onSelectItem: p => navRequest(p, dispatch),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(AppLayout);
