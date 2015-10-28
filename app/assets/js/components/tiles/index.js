import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import Tile from './Tile';

import moment from 'moment';

export let list = (props) => (
  <Tile {...props}>
    <ul>
      {props.items.map((item) => <ListTileItem {...item} />)}
    </ul>
  </Tile>
);

export let text = (props) => (
  <Tile {...props} className={props.className + " tile--text-btm"}>
    <span className="tile__callout">{props.callout}</span>
    <span className="tile__text">{props.text}</span>
  </Tile>
);

export let count = (props) => (
  <text {...props} callout={props.items.length} text={props.word}/>
);

let ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank">
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{moment(props.date).fromNow()}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);
