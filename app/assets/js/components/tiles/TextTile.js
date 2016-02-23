import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';

import TileContent from './TileContent';

export default class TextTile extends TileContent {

  constructor(props) {
    super(props);

    this.state = {
      itemIndex: 0,
    };

    this.onClickLink = this.onClickLink.bind(this);
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

  getBody(content) {
    const itemsToDisplay = this.props.zoomed ?
      content.items : [content.items[this.state.itemIndex]];

    return (
      <ReactCSSTransitionGroup
        transitionName="text-tile"
        transitionEnterTimeout={1000}
        transitionLeaveTimeout={1000}
      >
        {itemsToDisplay.map(item => {
          const tileItem = (<div key={item.id} className="tile__item">
            <span className="tile__callout">{item.callout}</span>
            <span className="tile__text">{item.text}</span>
          </div>);

          return item.href ?
            <a href={item.href} target="_blank" onClick={ this.onClickLink }>{tileItem}</a>
            : tileItem;
        }) }
      </ReactCSSTransitionGroup>
    );
  }

  onClickLink(e) {
    e.stopPropagation();

    if (this.props.editingAny) {
      e.preventDefault();
    }
  }

  static canZoom(content) {
    if (content && content.items) {
      return content.items.length > 1;
    }
    return false;
  }

}
