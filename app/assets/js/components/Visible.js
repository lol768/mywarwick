import React from 'react';
import * as PropTypes from 'prop-types';
import Observable from './system/Observable';

export default class Visible extends React.PureComponent {
  static propTypes = {
    visible: PropTypes.bool.isRequired,
  };

  static childContextTypes = {
    visibility: PropTypes.object,
  };

  constructor() {
    super();
    this.observable = new Observable();
  }

  getChildContext() {
    return {
      visibility: this.observable,
    };
  }

  componentWillMount() {
    if (this.props.visible) {
      this.observable.set('willShow');
    }
  }

  componentDidMount() {
    if (this.props.visible) {
      this.observable.set('didShow');
    }
  }

  componentWillUpdate(nextProps) {
    if (this.props.visible !== nextProps.visible) {
      this.observable.set(nextProps.visible ? 'willShow' : 'willHide');
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.visible !== prevProps.visible) {
      this.observable.set(this.props.visible ? 'didShow' : 'didHide');
    }
  }

  render() {
    return (<div className={this.props.visible ? '' : 'hidden'}>
      { this.props.children }
    </div>);
  }
}

