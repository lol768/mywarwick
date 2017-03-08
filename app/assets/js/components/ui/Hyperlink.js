import React, { PropTypes } from 'react';

/**
 * Wraps text in hyperlink if href is defined and not null
 * @param href URL for link, null for no hyperlink
 * @param child to wrap inside hyperlink
 */
const Hyperlink = (props) => {
  const child = props.children;
  const el = React.isValidElement(child) ? child : <span>{ child }</span>;
  return props.href ?
    <a href={ props.href } target="_blank" {...props}>{ child }</a>
    : el;
};

export default Hyperlink;

Hyperlink.displayName = 'Hyperlink';
Hyperlink.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
    PropTypes.element,
  ]),
  href: PropTypes.string,
};

