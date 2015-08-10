
const React = require('react/addons');

export default class TilePanel extends React.Component {
  constructor(props) {
    super(props);
  }
  render() {
    return <div className="col-md-3 col-sm-4">
      <div className="card card-default">
        <div className="card-heading">{this.props.heading}</div>
        <div className="card-content">
          {this.props.children}
        </div>
      </div>
    </div>;
  }
}