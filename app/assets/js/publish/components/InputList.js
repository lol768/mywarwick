import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';

export class InputList extends React.PureComponent {
  static propTypes = {
    name: PropTypes.string,
    picker: PropTypes.func,
    items: PropTypes.arrayOf(PropTypes.object),
    handleChange: PropTypes.func,
    placeholderText: PropTypes.string,
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
    const { items, handleChange, type } = this.props;
    if (!items.filter(item => item.value === newItem.value).length > 0) {
      handleChange({ items: [...items, newItem] }, type);
    }
  }

  removeItem(value) {
    const { items, handleChange, type } = this.props;
    handleChange({ items: items.filter(item => item.value !== value) }, type);
  }

  render() {
    const { picker, name, items, placeholderText } = this.props;

    return (
      <div>
        <InputSearch
          name={name}
          initPicker={picker}
          addItem={this.addItem}
          placeholderText={placeholderText}
        />
        <ul className="list-unstyled">
          {
            items.map(({ value, text }) => (
              <ListItem
                key={value}
                name={name}
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
        className="form-control input-search"
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
    className: PropTypes.string,
  };

  static defaultProps = {
    className: 'list-item',
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
      <li className={this.props.className}>
        <input name={this.props.name} value={this.props.value} readOnly hidden />
        <span>{this.props.text}</span>
        <span>
          <a
            role="button"
            className="btn btn-xs btn-danger list-item__remove"
            onClick={this.removeItem}
            title="Clear"
            tabIndex={0}
          >&nbsp;Remove</a>
        </span>
      </li>
    );
  }
}

class ListItemWithOptions extends ListItem {
  static propTypes = {
    toggleOption: PropTypes.func,
    options: PropTypes.arrayOf(PropTypes.object),
  };

  constructor(props) {
    super(props);
    this.toggleOption = this.toggleOption.bind(this);
  }

  toggleOption({ target: { value } }) {
    const oldOption = this.props.options.find(opt => opt[value])[value];
    const updatedOption = { [value]: { ...oldOption, selected: !oldOption.selected } };
    const newOptions =
      [...this.props.options.filter(opt => opt[value] === undefined), updatedOption];
    this.props.toggleOption(this.props.value, newOptions);
  }

  render() {
    const optBtn = option =>
      _.map(option, (val, key) => (
        <div className="checkbox--list-item-option" key={key}>
          <label className="control-label">
            <input
              className="form-check"
              type="checkbox"
              name={this.props.value}
              value={key}
              checked={val.selected}
              onChange={this.toggleOption}
            />
            {`${val.students.length} ${val.studentRole}(s)`}
          </label>
        </div>
      ))[0];

    return (
      <li>
        <input name={this.props.name} value={this.props.value} readOnly hidden />
        <span>{this.props.text}</span>&nbsp;
        {this.props.options.map(optBtn)}
        <span className="pull-right">
          <a
            role="button"
            className="btn btn-xs btn-danger list-item__remove"
            onClick={this.removeItem}
            title="Clear"
            tabIndex={0}
          >&nbsp;Remove</a>
        </span>
      </li>
    );
  }
}


export class InputOptionsList extends InputList {
  constructor(props) {
    super(props);
    this.state = { error: null };
    this.toggleOption = this.toggleOption.bind(this);
  }

  addItem(newItem) {
    const { items, handleChange, type } = this.props;
    if (newItem.options.length === 0) {
      this.setState({ error: 'That staff member has no students to add' });
    } else if (!items.filter(item => item.value === newItem.value).length > 0) {
      handleChange({ items: [...items, newItem] }, type);
      this.setState({ error: null });
    } else {
      this.setState({ error: 'That staff member has already been added' });
    }
  }

  toggleOption(value, options) {
    const { items, handleChange, type } = this.props;
    const updatedItem = { ...items.find(i => i.value === value), options };
    handleChange({ items: [...items.filter(i => i.value !== value), updatedItem] }, type);
  }

  render() {
    const { picker, name, items, placeholderText } = this.props;
    return (
      <div>
        <InputSearch
          name={name}
          initPicker={picker}
          addItem={this.addItem}
          placeholderText={placeholderText}
        />
        {this.state.error ? <div className="error-msg">{this.state.error}</div> : null}
        <ul className="list-unstyled">
          {items.map(({ value, text, options }) => (
            <ListItemWithOptions
              key={value}
              name={name}
              value={value}
              text={text}
              options={options}
              removeItem={this.removeItem}
              toggleOption={this.toggleOption}
            />
          ))
          }
        </ul>
      </div>
    );
  }
}
