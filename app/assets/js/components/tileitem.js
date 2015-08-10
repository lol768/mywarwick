
const React = require('react/addons');

export default class TileItem extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return <li className="card-item">
      {this.props.children}
    </li>
  }
}
