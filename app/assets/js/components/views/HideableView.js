/* eslint no-unused-vars:0 */
import React, { PropTypes } from 'react';
import shallowCompare from 'shallow-compare';

export default class HideableView extends React.Component {

  static propTypes = {
    hiddenView: PropTypes.bool.isRequired,
  };

  shouldComponentUpdate(newProps, newState) {
    if (this.props.hiddenView || newProps.hiddenView) {
      // it was hidden or will be hidden. Even if there were any changes
      // we wouldn't see them
      return false;
    } else {
      // otherwise mimic PureComponent
      return shallowCompare(this, newProps, newState);
    }
  }

  componentWillMount() {
    if (!this.props.hiddenView) {
      this.componentWillShow(true);
    }
  }

  componentDidMount() {
    if (!this.props.hiddenView) {
      this.componentDidShow(true);
    }
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextProps.hiddenView && !this.props.hiddenView) {
      this.componentWillHide(false, nextProps, nextState);
    } else if (!nextProps.hiddenView && this.props.hiddenView) {
      this.componentWillShow(false, nextProps, nextState);
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevProps.hiddenView && !this.props.hiddenView) {
      this.componentDidShow(false, prevProps, prevState);
    } else if (!prevProps.hiddenView && this.props.hiddenView) {
      this.componentDidHide(prevProps, prevState);
    }
  }

  componentWillUnmount() {
    this.componentWillHide(true);
  }

  componentWillShow(isMount, nextProps, nextState) {
    // Override this to mimic WillMount or WillUpdate
    // nextProps/State will be undefined if isMount
  }

  componentDidShow(isMount, prevProps, prevState) {
    // Override this to mimic DidMount or DidUpdate
    // prevProps/State will be undefined if isMount
  }

  componentWillHide(isUnmount, nextProps, nextState) {
    // Override this to mimic WillUnmount or WillUpdate
    // nextProps/State may be undefined (if called on unmount)
  }

  componentDidHide(prevProps, prevState) {
    // Override this to mimic DidUpdate
  }

}
