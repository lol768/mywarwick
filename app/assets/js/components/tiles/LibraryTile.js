import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import Hyperlink from '../ui/Hyperlink';
import TextTile from './TextTile';
import _ from 'lodash-es';

export default class LibraryTile extends TextTile {

  renderItems(items) {
    return items.map(item =>
      <Hyperlink key={item.id} href={item.href}>
        <div key={item.id} className="tile__item">
          <span className="tile__text">
            {item.dueMessage}: {item.itemTitle}
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
        {this.getSubtitle()}
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
        {this.getSubtitle()}
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
        {this.getSubtitle()}
        {this.renderItems([content.items[this.state.itemIndex]])}
      </ReactCSSTransitionGroup>
    );
  }

  getSubtitle() {
    const { content } = this.props;
    let subtitleArray = ['', ''];
    if (content.subtitle) subtitleArray = content.subtitle.split(' ');
    return (
      <span>
        <span className="tile__callout">
          {subtitleArray[0]}
        </span>
        &nbsp;{subtitleArray.slice(1)}
      </span>
    );
  }
}

