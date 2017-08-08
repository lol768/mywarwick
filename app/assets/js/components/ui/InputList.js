import React from 'react';
import * as PropTypes from 'prop-types';

export default class InputList extends React.PureComponent {

  constructor(props) {
    super(props);
    this.state = {
      items: []
    };
    this.addItem = this.addItem.bind(this);
  }

  addItem(item) {
    this.setState(({ items }) =>
      items.concat([item])
    )
  }

  render() {

    return (
      <div>
        <InputList addItem={this.addItem}/>
        <ul>
          {
            this.state.items.map(({ value, text }) => (
              <ListItem key={value} text={text}/>
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

  handleTextChange({ target: { value } }) {
    console.log(value);
  }

  addItem() {
    this.props.addItem(this.state.value);
    setState(InputSearch.initialState);
  }

  render() {
    return (
      <div>
        <input
          type="text"
          value={this.state.value}
          onChange={this.handleTextChange}
        />
        <button onClick={this.addItem}>add</button>
      </div>
    )
  }
}

class ListItem extends React.PureComponent {

  static propTypes = {
    value: PropTypes.string,
    text: PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <li>{this.props.text}</li>
    )
  }

}