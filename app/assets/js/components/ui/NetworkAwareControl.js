import * as React from 'react';
import * as PropTypes from 'prop-types';

export default class NetworkAwareControl extends React.PureComponent {
  static propTypes = {
    loading: PropTypes.bool,
    failure: PropTypes.bool,
    children: PropTypes.node,
  };

  render() {
    if (this.props.loading) {
      return (
        <i className="fal fa-spinner fa-pulse" />
      );
    }

    if (this.props.failure) {
      return (
        <i className="fal fa-exclamation-triangle" />
      );
    }

    return this.props.children;
  }
}
