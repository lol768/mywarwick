import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';

export default class MultilineTextInput extends React.PureComponent {
  static defaultProps = {
    rows: 3,
    cols: 50,
    items: [],
  };

  static propTypes = {
    name: PropTypes.string,
    formPath: PropTypes.string,
    handleChange: PropTypes.func.isRequired,
    placeholder: PropTypes.string,
    rows: PropTypes.number,
    cols: PropTypes.number,
    items: PropTypes.arrayOf(PropTypes.string),
    type: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.handleTextChange = this.handleTextChange.bind(this);
  }

  handleTextChange({ target: { value } }) {
    const items = _.filter(value.split(','), val => val.length > 4);
    this.props.handleChange(items, this.props.type);
  }

  render() {
    return (
      <textarea
        className="form-control"
        onChange={this.handleTextChange}
        name={this.props.items.length ? this.props.name : ''} // don't appear as submittable form field if no value
        placeholder={this.props.placeholder}
        rows={this.props.rows}
        cols={this.props.cols}
        defaultValue={this.props.items.join(', ')}
      />
    );
  }
}
