import React from 'react';

import formatDate from '../../dateFormatter';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import Tile from './Tile';

import _ from 'lodash';

export default class AgendaTile extends Tile {

  getBody(content) {
    let maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    let itemsToDisplay = this.isZoomed() ? content.items : _.take(content.items, maxItemsToDisplay);

    let events = itemsToDisplay.map(event => <AgendaTileItem {...event}/>);

    return (
      <GroupedList orderDescending={true} groupBy={groupItemsByDate}>
        {events}
      </GroupedList>
    );
  }

}

let AgendaTileItem = (props) => (
  <div className={classNames("agenda-item", "row")}>
    <div className="col-sm-3">
      {formatDate(props.date)}
    </div>
    <div className="col-sm-9">
      {props.title}
    </div>
  </div>
);
