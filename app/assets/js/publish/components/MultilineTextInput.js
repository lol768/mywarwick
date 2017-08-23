import React from 'react';
import * as PropTypes from 'prop-types';

export default class MultilineTextInput extends React.PureComponent {
  static defaultProps = {
    rows: 3,
    cols: 50,
  };

  static propTypes = {
    name: PropTypes.string.isRequired,
    handleChange: PropTypes.func.isRequired,
    placeholder: PropTypes.string,
    rows: PropTypes.number,
    cols: PropTypes.number,
  };

  constructor(props) {
    super(props);
    this.handleTextChange = this.handleTextChange.bind(this);
  }

  handleTextChange(event) {
    this.props.handleChange(event.target.value, this.props.name);
  }

  render() {
    return (
      <textarea
        className="form-control"
        onChange={this.handleTextChange}
        name={this.props.name}
        placeholder={this.props.placeholder}
        rows={this.props.rows}
        cols={this.props.cols}
      />
    );
  }
}
