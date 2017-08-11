import React from 'react';
import * as PropTypes from 'prop-types';
import $ from 'jquery';

export default class InputList extends React.PureComponent {

  static propTypes = {
    items: PropTypes.arrayOf(PropTypes.object)
  };

  constructor(props) {
    super(props);
    this.state = {
      items: props.items || []
    };
    this.addItem = this.addItem.bind(this);
    this.removeItem = this.removeItem.bind(this);
  }

  addItem(newItem) {
    if (!this.state.items.filter(item => item.value === newItem.value).length > 0)
      this.setState(({ items }) => ({ items: items.concat([newItem]) }));
  }

  removeItem(value) {
    this.setState(({ items }) => (
      { items: items.filter(item => item.value !== value) })
    );
  }

  render() {

    return (
      <div>
        <InputSearch name={this.props.name} addItem={this.addItem}/>
        <ul className="list-unstyled">
          {
            this.state.items.map(({ value, text }) => (
              <ListItem key={value} value={value} text={text} removeItem={this.removeItem}/>
            ))
          }
        </ul>
      </div>
    )
  }
}


class InputSearch extends React.PureComponent {

  static propTypes = {
    addItem: PropTypes.func
  };

  static initialState = { value: '' };

  constructor(props) {
    super(props);
    this.state = InputSearch.initialState;
    this.handleTextChange = this.handleTextChange.bind(this);
  }

  componentDidMount() {
    $(this.refs.modulePicker).modulePicker({
      addItem: this.props.addItem
    });
  }

  handleTextChange({ target: { value } }) {
    this.setState({ value })
  }

  render() {
    return (

      <input
        placeholder="Search for modules"
        name={this.props.name}
        ref="modulePicker"
        value={this.state.value}
        onChange={this.handleTextChange}
      />
    )
  }
}

class ListItem extends React.PureComponent {

  static propTypes = {
    value: PropTypes.string,
    text: PropTypes.string,
    removeItem: PropTypes.func
  };

  constructor(props) {
    super(props);
    this.removeItem = this.removeItem.bind(this);
  }

  removeItem(event) {
   event.preventDefault();
   this.props.removeItem(this.props.value)
  }

  render() {
    return (
      <li>
        <span>{this.props.text}</span>
        <span>
          <a href="#"
             className="clear-field"
             onClick={this.removeItem}
             title="Clear">&nbsp;&times;</a>
        </span>
      </li>
    )
  }

}