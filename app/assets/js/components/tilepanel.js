

import React from 'react/addons';

export default class TilePanel extends React.Component {
  constructor(props) {
    super(props);
  }
  render() {
    let headingElement = null;
    if (this.props.heading) {
      headingElement = <div className="card-heading">{this.props.heading}</div>
    }

    return <div className="col-md-4 col-sm-6">
      <div className="card card-default">
        {headingElement}
        <div className="card-content">
          {this.props.children}
        </div>
      </div>
    </div>;
  }
}
