import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';

import Tile from './Tile';

import moment from 'moment';

export let list = (props) => (
  <Tile {...props}>
    <ul>
      {props.items.map((item) => <ListTileItem {...item} />)}
    </ul>
  </Tile>
);

export class text extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      itemIndex: 0
    };
  }

  componentDidMount() {
    let interval = setInterval(this.onInterval.bind(this), 5000);

    this.setState({
      transitionInterval: interval
    });
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
    let itemsToDisplay = [this.props.items[this.state.itemIndex]];

    let items = itemsToDisplay.map((item) => (
      <div className="tile__item" key={item.key}>
        <span className="tile__callout">{item.callout}</span>
        <span className="tile__text">{item.text}</span>
      </div>
    ));

    return (
      <Tile {...this.props} className={this.props.className + " tile--text-btm"}>
        <ReactCSSTransitionGroup transitionName="text-tile"
                                 transitionEnterTimeout={1000}
                                 transitionLeaveTimeout={1000}>
          {items}
        </ReactCSSTransitionGroup>
      </Tile>
    );
  }

}

export let count = (props) => (
  <text {...props} callout={props.items.length} text={props.word}/>
);

let ListTileItem = (props) => (
  <li className="list-tile-item">
    <a href={props.href} target="_blank">
      <span className="list-tile-item__title">{props.title}</span>
      { props.date ? <span className="list-tile-item__date">{moment(props.date).fromNow()}</span> : null }
      <span className="list-tile-item__text">{props.text}</span>
    </a>
  </li>
);
