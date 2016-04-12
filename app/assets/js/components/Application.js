import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import TabBar from './ui/TabBar';
import TabBarItem from './ui/TabBarItem';
import ID7Layout from './ui/ID7Layout';
import Immutable from 'immutable';
import { connect } from 'react-redux';
import { getNumItemsSince } from '../stream';
import { mq } from 'modernizr';
import $ from 'jquery';
import store from '../store';
import { registerReducer } from '../reducers';
import { push } from 'react-router-redux';

const isDesktop = () => mq('only all and (min-width: 768px)');

const isFourColumnLayout = () => mq('only all and (min-width: 992px)');

const initialState = Immutable.fromJS({
  className: undefined,
  isFourColumnLayout: false,
  colourTheme: 'default',
});

window.Store = store;

registerReducer('ui', (state = initialState, action) => {
  switch (action.type) {
    case 'ui.class':
      return state.set('className', action.className);
    case 'ui.layout':
      return state.set('isFourColumnLayout', action.isFourColumnLayout);
    case 'ui.theme':
      return state.set('colourTheme', action.theme);
    default:
      return state;
  }
});

export function updateColourTheme(theme) {
  return {
    type: 'ui.theme',
    theme,
  };
}

export function updateUIContext() {
  return (dispatch, getState) => {
    const currentClassName = getState().getIn(['ui', 'className']);
    if (currentClassName === undefined || isDesktop() !== (currentClassName === 'desktop')) {
      dispatch({
        type: 'ui.class',
        className: isDesktop() ? 'desktop' : 'mobile',
      });
    }

    if (isFourColumnLayout() !== getState().getIn(['ui', 'isFourColumnLayout'])) {
      dispatch({
        type: 'ui.layout',
        isFourColumnLayout: isFourColumnLayout(),
      });
    }
  };
}

store.dispatch(updateUIContext());
$(() => $(window).on('resize', () => store.dispatch(updateUIContext())));

class Application extends ReactComponent {

  constructor() {
    super();
    this.onSelectItem = this.onSelectItem.bind(this);
  }

  onSelectItem(p) {
    this.props.dispatch(push(p));
  }

  render() {
    const { location, notificationsCount, layoutClassName, user, children }
      = this.props;

    return (
      <ID7Layout path={ location.pathname }>
        { children }
        { layoutClassName === 'mobile' ?
          <TabBar selectedItem={ location.pathname } onSelectItem={ this.onSelectItem }>
            <TabBarItem title="Me" icon="user" path="/"/>
            <TabBarItem
              title="Notifications" icon="inbox" path="/notifications"
              badge={ notificationsCount } isDisabled = { !user.authenticated }
            />
            <TabBarItem
              title="Activity" icon="dashboard" path="/activity"
              isDisabled = { !user.authenticated }
            />
            <TabBarItem title="News" icon="mortar-board" path="/news"/>
            <TabBarItem title="Search" icon="search" path="/search"/>
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

export default connect(mapStateToProps)(Application);
