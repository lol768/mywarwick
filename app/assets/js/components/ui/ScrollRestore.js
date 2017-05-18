import React, { PropTypes } from 'react';
import { attachScrollRestore, detachScrollRestore } from '../../state/ui';

export default class ScrollRestore extends React.Component {

  static propTypes = {
    url: PropTypes.string.isRequired,
    forceTop: PropTypes.bool,
    children: PropTypes.node,
  };

  componentDidMount() {
    if (this.props.forceTop) {
      window.scrollTo(0, 0);
    } else {
      attachScrollRestore(this.props.url);
    }
  }

  componentWillUnmount() {
    detachScrollRestore(this.props.url);
  }

  render() {
    return this.props.children;
  }

}
