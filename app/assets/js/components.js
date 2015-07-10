
var React = require('react');
var moment = require('moment');

//const DATE_FORMAT = ;

export class WeatherTile extends React.Component {
  constructor(props) {
    super(props);
    this.state = {temperature:'-'};
    this.props.stream.subscribe(
      (msg) => {
        this.state.temperature = msg.temperature;
        this.setState(this.state);
      },
      (err) => {}
    );
  }

  render() {
    "use strict";
    return <TilePanel heading={"Weather - " + this.props.location}>
      <h2><i className="fa fa-sun-o"></i>{this.state.temperature}&deg;C</h2>
    </TilePanel>
  }
}

class TilePanel extends React.Component {
  render() {
    return <div className="col-md-3 col-sm-4">
      <div className="panel panel-default">
        <div className="panel-heading">{this.props.heading}</div>
        <div className="panel-body">
          {this.props.children}
        </div>
      </div>
    </div>;
  }
}

/**
 * Renders a single value
 */
export class ValueTile extends React.Component {
  constructor(props) {
    super(props);
    this.state = {value:''};
    this.props.stream.subscribe(
      (msg) => {
        this.state.value = msg.value;
        this.setState(this.state);
      },
      (err) => {}
    );
  }

  render() {
    return <TilePanel heading={this.props.title}>
      <h3>{this.state.value}</h3>
    </TilePanel>;
  }
}

export class ActivityStreamTile extends React.Component {
  constructor(props) {
    super(props);
    this.state = { items: [] };
    this.props.stream.subscribe(
      (msg) => {
        var items = this.state.items;
        items.push.apply(items, msg.items || []);
        this.setState(this.state);
      },
      (err) => {}
    );
  }

  renderItem(item) {
    return <li key={item.key}>
      <span className="title">{item.title}</span>
      <span className="published" title={item.published}>{moment(item.published).fromNow()}</span>
    </li>
  }

  render() {
    var items = _.chain(this.state.items).slice(-5).reverse().value();
    return <TilePanel heading={this.props.title}>
      <ul className="activity-stream">
        {items.map(this.renderItem)}
      </ul>
    </TilePanel>;
  }
}

export class TileApp extends React.Component {

  render() {
    var ds = this.props.datasource;
    return <div className="tiles row">
      {this.props.children}
    </div>;
  }
}