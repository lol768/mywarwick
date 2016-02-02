import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class CheckableListItem extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      checked: this.props.checked || false,
    };
  }

  toggle(e) {
    e.preventDefault();

    this.setState({
      checked: !this.state.checked,
    });
  }

  render() {
    return (
      <a href="#" className="list-group-item" onClick={this.toggle.bind(this)}>
        <span className="media">
          <span className="media-left">
            <i
              className={ `fa fa-fw fa-${this.state.checked ? 'check-square' : 'square'}` }
              style={{ color: this.props.color }}
            > </i>
          </span>
          <span className="media-body">
            {this.props.text}
          </span>
        </span>
      </a>
    );
  }

}
