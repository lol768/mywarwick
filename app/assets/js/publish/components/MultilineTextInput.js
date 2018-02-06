import React from 'react';
import * as PropTypes from 'prop-types';

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
    valuePrefix: PropTypes.string,
    className: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.handleTextChange = this.handleTextChange.bind(this);
  }

  handleTextChange({ target: { value } }) {
    this.props.handleChange(value.split('\n'), this.props.type);
  }

  render() {
    const itemString = this.props.items.join('\r\n');

    return (
      <div className={this.props.className}>
        <textarea
          name={this.props.items.length ? this.props.name : ''} // don't appear as submittable form field if no value
          hidden
          readOnly
          value={`${this.props.valuePrefix}${itemString}`}
        />
        <textarea
          className="form-control"
          onChange={this.handleTextChange}
          placeholder={this.props.placeholder}
          rows={this.props.rows}
          cols={this.props.cols}
          defaultValue={itemString}
        />
      </div>
    );
  }
}
