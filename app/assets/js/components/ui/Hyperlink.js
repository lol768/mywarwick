import React from 'react';
import * as PropTypes from 'prop-types';
import { push } from 'react-router-redux';
import { Routes } from '../AppRoot';
import store from '../../store';

const campusMapBaseUrl = 'https://campus.warwick.ac.uk';

/**
 * Wraps text in hyperlink if href is defined and not null
 * If the href is a map link handle it internally
 * @param href URL for link, null for no hyperlink
 * @param child to wrap inside hyperlink
 */
export default class Hyperlink extends React.PureComponent {
  constructor(props) {
    super(props);

    this.onClick = this.onClick.bind(this);
  }

  onClick(e) {
    const { href } = this.props;
    if (href && href.indexOf(campusMapBaseUrl) === 0 && href.indexOf('?') >= 0) {
      e.preventDefault();
      store.dispatch(push(`/${Routes.TILES}/map/${href.substr(href.indexOf('?'))}`));
    }
  }

  render() {
    const child = this.props.children;
    const el = React.isValidElement(child) ? child : <span>{child}</span>;
    return this.props.href
      ? <a onClick={this.onClick}
           href={this.props.href}
           target="_blank"
           rel="noopener noreferrer"
           {...this.props}>{child}</a>
      : el;
  }
}

Hyperlink.displayName = 'Hyperlink';
Hyperlink.propTypes = {
  children: PropTypes.node,
  href: PropTypes.string,
};
