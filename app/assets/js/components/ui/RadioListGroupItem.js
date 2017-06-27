import React from 'react';
import * as PropTypes from 'prop-types';

export default class RadioListGroupItem extends React.PureComponent {

  static propTypes = {
    icon: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
    name: PropTypes.string,
    value: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    checked: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    this.props.onClick(this.props.value, this.props.name);
  }

  render() {
    return (
      <div className="list-group-item" onClick={ this.onClick }>
        <div className="media">
          <div className="media-left">
            <i className={ `fa fa-fw fa-${this.props.icon}` } />
          </div>
          <div className="media-body">
            { this.props.description }
          </div>
          <div className="media-right">
            <div className="media-right">
              <input type="radio" checked={ this.props.checked } readOnly />
            </div>
          </div>
        </div>
      </div>
    );
  }

}
