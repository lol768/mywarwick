import React, { cloneElement } from 'react';
import ReactComponent from 'react/lib/ReactComponent';


export default class TabBar extends ReactComponent {

  getChildren() {
    return this.props.children.map((el) => (
      cloneElement(el, {
        key: el.props.title,
        ref: el.props.title.toLowerCase(),
        active: el.props.path === this.props.selectedItem,
        onClick: () => this.props.onSelectItem(el.props.path),
        badge: el.props.badge,
        icon: el.props.icon,
      })
    ));
  }

  render() {
    return (
      <nav className="tab-bar">
        <ul className="tab-bar__tabs">
          { this.getChildren() }
        </ul>
      </nav>
    );
  }

}
