import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import classNames from 'classnames';
import { Hyperlink } from './utilities';

import _ from 'lodash';

import TileContent from './TileContent';

export default class TextTile extends TileContent {

  constructor(props) {
    super(props);

    this.state = {
      itemIndex: 0,
    };
  }

  componentWillUnmount() {
    this.clearTransitionInterval();
  }

  componentDidMount() {
    this.setTransitionInterval();
  }

  componentWillReceiveProps() {
    this.setTransitionInterval();
  }

  setTransitionInterval() {
    clearInterval(this.transitionInterval);

    if (this.props.content) {
      this.transitionInterval = setInterval(this.onInterval.bind(this), 5000);
    }
  }

  clearTransitionInterval() {
    clearInterval(this.transitionInterval);

    this.transitionInterval = null;
  }

  onInterval() {
    const oldItemIndex = this.state.itemIndex;

    const itemIndex = (oldItemIndex >= this.props.content.items.length - 1) ? 0 : oldItemIndex + 1;

    this.setState({
      itemIndex,
    });
  }

  mapTextItems(itemsToDisplay, className) {
    return itemsToDisplay.map(item => {
      const tileItem = (<div key={item.id} className={classNames('tile__item', className)}>
        <span className="tile__callout">{item.callout}</span>
        <span className="tile__text">{item.text}</span>
      </div>);

      return <Hyperlink key={item.href} href={item.href} >{ tileItem }</Hyperlink>;
    });
  }

  getZoomedBody() {
    const chunkedItems = _.chunk(this.mapTextItems(this.props.content.items, 'col-xs-6'), 2);

    return (
      <div className="container-fluid">
        {chunkedItems.map((children, i) => <div key={i} className="row">{children}</div>)}
      </div>
    );
  }

  getLargeBody() {
    const { content } = this.props;

    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, this.props.maxItemsToDisplay || 4);

    return (
      <div>
        {this.mapTextItems(itemsToDisplay)}
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
        {this.mapTextItems([content.items[this.state.itemIndex]])}
      </ReactCSSTransitionGroup>
    );
  }

  getSmallBody() {
    return this.getWideBody();
  }

  static canZoom(content) {
    if (content && content.items) {
      return content.items.length > 1;
    }

    return false;
  }

}
