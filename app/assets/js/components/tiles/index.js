import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import Tile from './Tile';

import moment from 'moment';

export let ListTile = (props) => (
  <Tile {...props}>
    <ul>
      {props.items.map((item) => <ListTileItem {...item} />)}
    </ul>
  </Tile>
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

export let TextTile = (props) => (
  <Tile {...props} className={props.className + " tile--text-btm"}>
    <span className="tile__callout">{props.callout}</span>
    <span className="tile__text">{props.text}</span>
  </Tile>
);

export let CountTile = (props) => (
  <TextTile {...props} callout={props.items.length} text={props.word} />
);

export default {
  list: ListTile,
  text: TextTile,
  count: CountTile
};