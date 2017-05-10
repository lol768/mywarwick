/* global PaymentRequest */
import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import Hyperlink from '../ui/Hyperlink';
import TextTile from './TextTile';
import _ from 'lodash-es';
import moment from 'moment';
import { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';

export default class LibraryTile extends TextTile {

  static extraTileSizes() {
    return [TILE_SIZES.LARGE, TILE_SIZES.TALL];
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES + LibraryTile.extraTileSizes();
  }

  renderItems(items) {
    return items
      .map(item => {
        switch (item.type) {
          case 'loan':
            return this.makeLoanItem(item);
          case 'hold':
            return this.makeHoldItem(item);
          default:
            return null;
        }
      }
    );
  }

  makeSubtitle(type, length) {
    return (
      <span>
        <span className="tile__callout">
          {`${length} ${this._pluralise(type, length)}`}
        </span>
      </span>
    );
  }

  makeSingleSubtitle(items) {
    const groupedItems = _.groupby(items, 'type');
    let text = '';
    _.forEach(groupedItems, (value, key) => {
      text += `${value.length} ${this._pluralise(key, value.length)} `;
    });

    const chunks = text.split(/\s+/);
    const titleArr = [chunks.shift(), chunks.join(' ')];

    return (
    <span>
      <span className="tile__callout">
        {titleArr[0]}
      </span>
      &nbsp;{titleArr[1]}
    </span>
    );
  }

  makeLoanItems(items) {
    return items
      .filter((e) => e.type === 'loan')
      .map(this.makeLoanItem);
  }

  makeLoanItem(item) {
    return (
      <Hyperlink key={item.id} href={item.href}>
        <div key={item.id} className="tile__item">
          <span className="tile__text">
            {item.recallDate ?
              `Recalled on ${
              moment(item.recallDate, 'YYYY-MM-DD').format('MMM Do YYYY')
            }, ${item.dueMessage.toLowerCase()}` :
              item.dueMessage}: {item.itemTitle}
            </span>
        </div>
      </Hyperlink>
    );
  }

  makeHoldItems(items) {
    return items
      .filter((e) => e.type === 'hold')
      .map(this.makeHoldItem);
  }

  makeHoldItem(item) {
    return (
      <Hyperlink key={item.id} href={item.href}>
        <div key={item.id} className="tile__item">
          <span className="tile__text">
            {item.status.toLowerCase().indexOf('ready') !== -1 ?
              `${item.status} at ${item.pickupLocation}` :
              item.status}: {item.itemTitle}
          </span>
        </div>
      </Hyperlink>
    );
  }

  getZoomedBody() {
    return this.getLargeBody();
  }

  getLargeBody() {
    const { content } = this.props;

    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, content.items.length);

    const loanItems = itemsToDisplay.filter((e) => e.type === 'loan');
    const holdItlems = itemsToDisplay.filter((e) => e.type === 'hold');

    return (
      <div>
        {this.makeSubtitle('loan', loanItems.length)}
        {this.makeLoanItems(loanItems)}
        {this.makeSubtitle('hold', holdItlems.length)}
        {this.makeHoldItems(holdItlems)}
      </div>
    );
  }

  getWideBody() {
    const { content } = this.props;
    return (
      <ReactCSSTransitionGroup
        className="text-tile-transition-group"
        transitionName="text-tile"
        transitionEnterTimeout={1000}
        transitionLeaveTimeout={1000}
      >
        {this.makeSingleSubtitle(content.items)}
        {this.renderItems([content.items[this.state.itemIndex]])}
      </ReactCSSTransitionGroup>
    );
  }

  _pluralise = (unit, len) => `${unit}${len === 1 ? '' : 's'}`;

}

