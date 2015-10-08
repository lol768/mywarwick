
const React = require('react/addons');
const log = require('loglevel');
const moment = require('moment');
const _ = require('lodash');

const TileItem = require('../../components/tileitem');
const TilePanel = require('../../components/tilepanel');

export default class ActivityStreamTile extends React.Component {
  constructor(props) {
    super(props);
    const key = props.tileId;
    this.state = {items:[]};
    props.store.get(key).then((data) => {
      if (data) this.setState(data);
    }).done();
    props.store.updates(key).subscribe(
      (data) => {
        this.setState(data);
      },
      log.error
    );
  }

  static renderItem(item) {
    return <TileItem key={item.id}>
      <span className="title">{item.title}</span>
      <span className="published" title={item.published}>{moment(item.published).fromNow()}</span>
    </TileItem>
  }

  //<ReactCSSTransitionGroup transitionName="activity">
  //{items.map(this.renderItem)}
  //</ReactCSSTransitionGroup>

  render() {
    //let ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
    let items = _.chain(this.state.items).slice(-10).value();
    let renderedItems = items.map(ActivityStreamTile.renderItem);
    return <TilePanel heading={this.props.title} contentClass="">
      <ul className="activity-stream">
        {renderedItems}
      </ul>
    </TilePanel>;
  }
}