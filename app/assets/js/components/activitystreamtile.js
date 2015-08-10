
const React = require('react/addons');
const TileItem = require('./tileitem');
const TilePanel = require('./tilepanel');
const log = require('loglevel');
const moment = require('moment');

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

  renderItem(item) {
    return <TileItem key={item.id}>
      <span className="title">{item.title}</span>
      <span className="published" title={item.published}>{moment(item.published).fromNow()}</span>
    </TileItem>
  }

  //<ReactCSSTransitionGroup transitionName="activity">
  //{items.map(this.renderItem)}
  //</ReactCSSTransitionGroup>

  render() {
    //var ReactCSSTransitionGroup = React.addons.CSSTransitionGroup;
    var items = _.chain(this.state.items).slice(-10).value();
    var renderedItems = items.map(this.renderItem);
    return <TilePanel heading={this.props.title} contentClass="">
      <ul className="activity-stream">
        {renderedItems}
      </ul>
    </TilePanel>;
  }
}