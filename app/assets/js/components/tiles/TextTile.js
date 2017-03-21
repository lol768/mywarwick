import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import Hyperlink from '../ui/Hyperlink';
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
    if (this.props.content && this.props.content.items) {
      this.setTransitionInterval();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (!nextProps.content || !nextProps.content.items
      || this.state.itemIndex >= nextProps.content.items.length) {
      // If the number of items changes such that the current item doesn't exist any more,
      // reset the index of the item we're currently looking at
      this.setState({
        itemIndex: 0,
      });
    }

    this.setTransitionInterval();
  }

  setTransitionInterval() {
    clearInterval(this.transitionInterval);

    if (this.props.content && this.props.content.items) {
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

  renderItems(itemsToDisplay) {
    return itemsToDisplay.map(item => {
      if (!item) {
        return null;
      }

      return (
        <Hyperlink key={item.id} href={item.href}>
          <div className="tile__item">
            <span className="tile__callout">{item.callout}</span>
            <span className="tile__text">{item.text}</span>
          </div>
        </Hyperlink>
      );
    });
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
      content.items : _.take(content.items, this.props.maxItemsToDisplay || 4);

    return (
      <div>
        {this.renderItems(itemsToDisplay)}
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
        {this.renderItems([content.items[this.state.itemIndex]])}
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
