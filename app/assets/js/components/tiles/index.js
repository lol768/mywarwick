import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';

import Tile from './Tile';

import moment from 'moment';
import _ from 'lodash';

export class list extends ReactComponent {

  render() {
    var content;
    // if there is tile data to display
    if (this.props.content)
      content = <ul>
        {this.props.content.items.map((item) => <ListTileItem {...item} />)}
      </ul>;
    else content = <em>loading tile data...</em>;
    return (
      <Tile ref="tile" {...this.props}>
        {content}
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

  componentDidMount() {
    let interval = setInterval(this.onInterval.bind(this), 5000);
    if (this.props.content) {
      this.setState({
        transitionInterval: interval
      });
    }
  }

  componentWillUnmount() {
    clearInterval(this.state.transitionInterval);

    this.setState({
      transitionInterval: null
    });
  }

  onInterval() {
    let oldItemIndex = this.state.itemIndex;

    let itemIndex = (oldItemIndex == this.props.items.length - 1) ? 0 : oldItemIndex + 1;

    this.setState({
      itemIndex: itemIndex
    });
  }

  render() {
    var content;
    // if there is tile data to display
    if (this.props.content) {
      let itemsToDisplay = this.props.zoomed ? this.props.content.items : [this.props.content.items[this.state.itemIndex]];

      content = itemsToDisplay.map((item) => (
        <div className="tile__item" key={item.key}>
          <span className="tile__callout">{item.callout}</span>
          <span className="tile__text">{item.text}</span>
        </div>
      ));
    } else content = <em>loading tile data...</em>;

    return (
      <Tile ref="tile" {...this.props} className={this.props.className + " tile--text-btm"}>
        <ReactCSSTransitionGroup transitionName="text-tile"
                                 transitionEnterTimeout={1000}
                                 transitionLeaveTimeout={1000}>
          {content}
        </ReactCSSTransitionGroup>
      </Tile>
    );
  }

}

export class count extends ReactComponent {

  render() {
    if (this.props.zoomed) {
      return (
        <Tile ref="tile" {...this.props}>
          <ul>
            {this.props.content.items.map((item) => <ListTileItem {...item} />)}
          </ul>
        </Tile>
      );
    } else {
      var content;
      // if there is tile data to display
      if (this.props.content) {
        content = <div className="tile__item">
          <span className="tile__callout">{this.props.content.count || (this.props.content.items.length)}</span>
          <span className="tile__text">{this.props.content.word}</span></div>
      } else content = <em>loading tile data...</em>;
      return (
        <Tile ref="tile" {...this.props} className={"tile--text-btm " + this.props.className}>
          {content}
        </Tile>
      );
    }
  }

}

let ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank">
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{moment(props.date).fromNow()}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);
