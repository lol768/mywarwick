import React from 'react';

const Link = (props) => (
  <li>
    <a className="link-block__item" href={props.href}>
      {props.children}
    </a>
  </li>
);

export default Link;
