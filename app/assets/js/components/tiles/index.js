import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';

import Tile from './Tile';

import formatDate from '../../dateFormatter';
import classNames from 'classnames';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import _ from 'lodash';

export class agenda extends ReactComponent {

  getContent() {
    if (this.props.content) {
      let maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
      let itemsToDisplay = this.props.zoomed ? this.props.content.items : this.props.content.items.slice(0, maxItemsToDisplay);

      let events = itemsToDisplay.map((event) => {
        return (
          <AgendaItem {...event}/>
        );
      });

      return (
        <GroupedList orderDescending={true} groupBy={groupItemsByDate}>
          {events}
        </GroupedList>
      )
    } else if (this.props.errors) {
      return <em>error: {this.props.errors[0].message}</em>;
    } else {
      return <em>loading tile data...</em>;
    }
  }

  render() {
    return (
      <Tile ref="tile" {...this.props}>
        {this.getContent()}
      </Tile>
    )
  }
}

export class list extends ReactComponent {

  getContent() {
    if (this.props.content) {
      // only show the first maxItemsToDisplay items (defaults to 3) if not zoomed
      let maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
      let itemsToDisplay = this.props.zoomed ? this.props.content.items : this.props.content.items.slice(0, maxItemsToDisplay);
      return <ul>
        {itemsToDisplay.map((item) => <ListTileItem {...item} />)}
      </ul>;
    } else if (this.props.errors) {
      return <em>error: {this.props.errors[0].message}</em>;
    } else {
      return <em>loading tile data...</em>;
    }
  }

  render() {
    return (
      <Tile ref="tile" {...this.props}>
        {this.getContent()}
      </Tile>
    );
  }
}

export class text extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      itemIndex: 0
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
    let oldItemIndex = this.state.itemIndex;

    let itemIndex = (oldItemIndex >= this.props.content.items.length - 1) ? 0 : oldItemIndex + 1;

    this.setState({
      itemIndex: itemIndex
    });
  }

  getContent() {
    if (this.props.content) {
      let itemsToDisplay = this.props.zoomed ? this.props.content.items : [this.props.content.items[this.state.itemIndex]];

      return (
        <ReactCSSTransitionGroup transitionName="text-tile"
                                 transitionEnterTimeout={1000}
                                 transitionLeaveTimeout={1000}>
          {itemsToDisplay.map((item) => {

            let tileItem = <div className="tile__item" key={item.key}>
              <span className="tile__callout">{item.callout}</span>
              <span className="tile__text">{item.text}</span>
            </div>;

            return (item.href) ?
              <a href={item.href} target="_blank" onClick={function(e){e.stopPropagation();}}>{tileItem}</a>
              : tileItem;
          })}
        </ReactCSSTransitionGroup>
      );
    } else if (this.props.errors) {
      return <em>error: {this.props.errors[0].message}</em>;
    } else {
      return <em>loading tile data...</em>;
    }
  }

  render() {
    return (
      <Tile ref="tile" {...this.props} className={this.props.className + " tile--text-btm"}>
        {this.getContent()}
      </Tile>
    );
  }

}

export class count extends ReactComponent {

  getContent() {
    if (this.props.content) {
      return (
        <div className="tile__item">
          <span className="tile__callout">{this.props.content.count || (this.props.content.items.length)}</span>
          <span className="tile__text">{this.props.content.word}</span>
        </div>
      );
    } else if (this.props.errors) {
      return <em>error: {this.props.errors[0].message}</em>;
    } else {
      return <em>loading tile data...</em>;
    }
  }

  getZoomedContent() {
    if (this.props.content) {
      return (
        <ul>
          {this.props.content.items.map((item) => <ListTileItem {...item} />)}
        </ul>
      );
    } else if (this.props.errors) {
      return <em>error: {this.props.errors[0].message}</em>;
    } else {
      return <em>loading tile data...</em>;
    }
  }

  render() {
    if (this.props.zoomed) {
      return (
        <Tile ref="tile" {...this.props}>
          {this.getZoomedContent()}
        </Tile>
      );
    } else {
      return (
        <Tile ref="tile" {...this.props} className={"tile--text-btm " + this.props.className}>
          {this.getContent()}
        </Tile>
      );
    }
  }

}

let AgendaItem = (props) => (
  <div className={classNames("agenda-item", "row")}>
    <div className="col-sm-3">
      {formatDate(props.date)}
    </div>
    <div className="col-sm-9">
      {props.title}
    </div>
  </div>
);

let ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank" onClick={function(e){e.stopPropagation();}}>
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{formatDate(props.date)}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);
