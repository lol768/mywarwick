import React, { PropTypes } from 'react';

/**
 * Wraps text in hyperlink if href is defined and not null
 * @param href URL for link, null for no hyperlink
 * @param child to wrap inside hyperlink
 */
export const Hyperlink = ({ child, href }) => (
  href ?
    <a href={ href } target="_blank" ref="a">{ child }</a>
    : child
);

Hyperlink.displayName = 'Hyperlink';
Hyperlink.propTypes = {
  child: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
    PropTypes.element,
  ]),
  href: PropTypes.string,
};

