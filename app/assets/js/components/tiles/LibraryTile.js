import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import classNames from 'classnames';
import Hyperlink from '../ui/Hyperlink';

import _ from 'lodash';

import TileContent from './TileContent';

export default class LibraryTile extends TileContent {

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

  mapItems(itemsToDisplay, className) {
    return itemsToDisplay.map(item => {
      const tileItem = (<div key={item.id} className={classNames('tile__item', className)}>
        <span className="tile__text">{item.dueMessage}: {item.itemTitle}</span>
      </div>);

      return <Hyperlink key={item.href} href={item.href}>{ tileItem }</Hyperlink>;
    });
  }

  mapItemsForZoomedBody(itemsToDisplay, className) {
    return itemsToDisplay.map(item => {
      const tileItem = (<div key={item.id} className={classNames('tile__item', className)}>
        <span className="tile__text">{item.dueMessage}: {item.itemTitle}</span>
      </div>);

      return <Hyperlink key={item.href} href={item.href}>{ tileItem }</Hyperlink>;
    });
  }

  getZoomedBody() {
    const items = _.chunk(this.mapItemsForZoomedBody(this.props.content.items, 'col-xs-6'), 2);

    return (
      <div className="container-fluid">
        {this.getSubtitle()}
        {items.map((children, i) => <div key={i} className="row">{children}</div>)}
      </div>
    );
  }

  getLargeBody() {
    const { content } = this.props;

    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, content.items.length);

    return (
      <div>
        {this.getSubtitle()}
        {this.mapItems(itemsToDisplay)}
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
        {this.getSubtitle()}
        {this.mapItems([content.items[this.state.itemIndex]])}
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

  getSubtitle() {
    const { content } = this.props;
    let subtitleArray = ['', ''];
    if (content.subtitle) subtitleArray = content.subtitle.split(' ');
    return (
      <span className="tile__callout">
        {subtitleArray[0]} <small>{subtitleArray.slice(1)}</small>
      </span>
    );
  }
}

