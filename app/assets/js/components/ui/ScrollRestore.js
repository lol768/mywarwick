import { PropTypes } from 'react';
import { attachScrollRestore, detachScrollRestore } from '../../state/ui';
import HideableView from '../views/HideableView';

export default class ScrollRestore extends HideableView {

  static propTypes = {
    url: PropTypes.string.isRequired,
    forceTop: PropTypes.bool,
    children: PropTypes.node,
  };

  componentDidShow() {
    if (this.props.forceTop) {
      window.scrollTo(0, 0);
    } else {
      attachScrollRestore(this.props.url);
    }
  }

  componentWillHide() {
    detachScrollRestore(this.props.url);
  }

  render() {
    return this.props.children;
  }

}
