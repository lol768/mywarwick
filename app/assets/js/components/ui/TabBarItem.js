import React, { PropTypes } from 'react';
import classNames from 'classnames';

const formatBadgeCount = (n) => (n > 99 ? '99+' : n);

export default class TabBarItem extends React.PureComponent {

  constructor() {
    super();
    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    if (!this.props.isDisabled) this.props.onSelectItem(this.props.path);
  }

  render() {
    return (
      <li
        className={ classNames({
          'tab-bar-item': true,
          'tab-bar-item--active': this.props.active,
          disabled: this.props.isDisabled,
        }) }
        onClick={ this.onClick }
        ref="li"
      >
        <i className={ `fa fa-${this.props.icon}` }>
          { (this.props.badge > 0) ?
            <span className="badge">{ formatBadgeCount(this.props.badge) }</span> :
            null
          }
        </i>
        <span className="tab-label">{ this.props.title }</span>
      </li>
    );
  }

  static propTypes = {
    onSelectItem: PropTypes.func,
    isDisabled: PropTypes.bool,
    active: PropTypes.bool,
    badge: PropTypes.number,
    icon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
  }

}
