import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import Hyperlink from '../ui/Hyperlink';
import TextTile from './TextTile';
import pluralize from 'pluralize';
import _ from 'lodash-es';

export default class LibraryTile extends TextTile {

  renderItems(items) {
    return items
      .map(item => {
        switch (item.type) {
          case 'loan':
            return this.makeLoanItem(item);
          case 'hold':
            return this.makeHoldItem(item);
        }
      }
    );
  }

  makeSubtitle(type, length) {
    return (
      <span>
        <span className="tile__callout">
          {`${length} ${pluralize(type, length)}`}
        </span>
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
            {item.dueMessage}: {item.itemTitle}
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
            {item.status}: {item.itemTitle}
          </span>
        </div>
      </Hyperlink>
    );
  }

  getZoomedBody() {
    const { content: { items } } = this.props;

    const elements = _.zip(items, this.renderItems(items))
      .map(([item, element]) => <div className="col-xs-6" key={item.id}>{element}</div>);

    const chunkedItems = _.chunk(elements, 2);

    return (
      <div className="container-fluid">
        {chunkedItems.map((children, i) => <div key={i} className="row">{children}</div>)}
      </div>
    );
  }

  getLargeBody() {
    const { content } = this.props;

    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, content.items.length);

    return (
      <div>
        {this.makeSubtitle('loan', itemsToDisplay.length)}
        {this.makeLoanItem(itemsToDisplay)}
        {this.makeSubtitle('recall', itemsToDisplay.length)}
        {this.makeHoldItems(itemsToDisplay)}
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
        {this.makeLoanItems([content.items[this.state.itemIndex]])}
        {this.makeHoldItems([content.items[this.state.itemIndex]])}
      </ReactCSSTransitionGroup>
    );
  }
}

