import React from 'react';
import * as PropTypes from 'prop-types';

export default class MultilineTextInput extends React.PureComponent {
  static defaultProps = {
    rows: 3,
    cols: 50,
  };

  static propTypes = {
    name: PropTypes.string,
    formPath: PropTypes.string,
    handleChange: PropTypes.func.isRequired,
    placeholder: PropTypes.string,
    rows: PropTypes.number,
    cols: PropTypes.number,
  };

  constructor(props) {
    super(props);
    // this.state = { value: '' };
    this.handleTextChange = this.handleTextChange.bind(this);
  }

  handleTextChange({ target: { value } }) {
    // this.setState({ value });
    this.props.handleChange(value, this.props.formPath);
  }

  render() {
    return (
      <textarea
        className="form-control"
        onChange={this.handleTextChange}
        name={this.props.name}
        // value={this.state.value}
        placeholder={this.props.placeholder}
        rows={this.props.rows}
        cols={this.props.cols}
      />
    );
  }
}
