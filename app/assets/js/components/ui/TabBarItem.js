import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import wrapKeyboardSelect from '../../keyboard-nav';

const formatBadgeCount = n => (n > 99 ? '99+' : n);

export default class TabBarItem extends React.PureComponent {
  static propTypes = {
    onSelectItem: PropTypes.func,
    isDisabled: PropTypes.bool,
    active: PropTypes.bool,
    badge: PropTypes.number,
    icon: PropTypes.string.isRequired,
    selectedIcon: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    path: PropTypes.string.isRequired,
  };

  constructor() {
    super();
    this.onClick = this.onClick.bind(this);
    this.listItem = null;
  }

  onClick(e) {
    wrapKeyboardSelect(() => {
      if (!this.props.isDisabled) this.props.onSelectItem(this.props.path);
    }, e);
  }

  render() {
    return (
      <li
        className={ classNames({
          'tab-bar-item': true,
          'tab-bar-item--active': this.props.active,
          disabled: this.props.isDisabled,
        }) }
        role="button"
        tabIndex={0}
        onClick={ this.onClick }
        onKeyUp={ this.onClick }
        ref={(li) => { this.listItem = li; }}
      >
        <i
          className={ this.props.active ?
            `fal fa-${this.props.selectedIcon}` :
            `fal fa-${this.props.icon}`
          }
        >
          { (this.props.badge > 0) ?
            <span className="badge">{ formatBadgeCount(this.props.badge) }</span> :
            null
          }
        </i>
        <span className="tab-label">{ this.props.title }</span>
      </li>
    );
  }
}
