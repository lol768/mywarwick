import React from 'react';
import * as PropTypes from 'prop-types';

export default class InputList extends React.PureComponent {
  static propTypes = {
    name: PropTypes.string,
    picker: PropTypes.func,
    items: PropTypes.arrayOf(PropTypes.object),
    handleChange: PropTypes.func,
  };

  static defaultProps = {
    items: [],
  };

  constructor(props) {
    super(props);
    this.addItem = this.addItem.bind(this);
    this.removeItem = this.removeItem.bind(this);
  }

  addItem(newItem) {
    const { items, handleChange, name } = this.props;
    if (!items.filter(item => item.value === newItem.value).length > 0) {
      handleChange({ items: items.concat([newItem]) }, null, name);
    }
  }

  removeItem(value) {
    const { items, handleChange, name } = this.props;
    handleChange({ items: items.filter(item => item.value !== value) }, null, name);
  }

  render() {
    return (
      <div>
        <InputSearch
          name={this.props.name}
          initPicker={this.props.picker}
          addItem={this.addItem}
        />
        <ul className="list-unstyled">
          {
            this.props.items.map(({ value, text }) => (
              <ListItem
                key={value}
                name={this.props.name}
                value={value}
                text={text}
                removeItem={this.removeItem}
              />
            ))
          }
        </ul>
      </div>
    );
  }
}


class InputSearch extends React.PureComponent {
  static propTypes = {
    addItem: PropTypes.func,
    initPicker: PropTypes.func,
    placeholderText: PropTypes.string,
  };

  static defaultProps = {
    placeholderText: '',
  };

  static initialState = { value: '' };

  constructor(props) {
    super(props);
    this.state = InputSearch.initialState;
    this.handleTextChange = this.handleTextChange.bind(this);
  }

  componentDidMount() {
    this.props.initPicker.call(this.picker, { addItem: this.props.addItem });
  }

  handleTextChange({ target: { value } }) {
    this.setState({ value });
  }

  render() {
    return (
      <input
        placeholder={this.props.placeholderText}
        ref={picker => (this.picker = picker)}
        value={this.state.value}
        onChange={this.handleTextChange}
      />
    );
  }
}

class ListItem extends React.PureComponent {
  static propTypes = {
    value: PropTypes.string,
    text: PropTypes.string,
    removeItem: PropTypes.func,
    name: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.removeItem = this.removeItem.bind(this);
  }

  removeItem(event) {
    event.preventDefault();
    this.props.removeItem(this.props.value);
  }

  render() {
    return (
      <li>
        <input name={this.props.name} value={this.props.value} readOnly hidden />
        <span>{this.props.text}</span>
        <span>
          <a
            role="button"
            className="clear-field"
            onClick={this.removeItem}
            title="Clear"
            tabIndex={0}
          >&nbsp;&times;</a>
        </span>
      </li>
    );
  }
}
