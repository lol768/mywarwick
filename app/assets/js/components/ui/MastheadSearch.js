import React from 'react';
import ReactDOM from 'react-dom';
import Popover from './Popover';
import SearchField from './SearchField';
import SearchView from '../views/SearchView';
import $ from 'jquery';

export default class MastheadSearch extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      query: '',
      hasFocus: false,
    };

    this.onChange = this.onChange.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onSearch = this.onSearch.bind(this);
    this.onDocumentClick = this.onDocumentClick.bind(this);
  }

  componentWillUnmount() {
    $(document).off('click', this.onDocumentClick);
  }

  onChange(query) {
    if (query.trim().length === 0) {
      this.setState({ query });
    }
  }

  onSearch(query) {
    this.setState({ query });
  }

  onDocumentClick(e) {
    const thisNode = ReactDOM.findDOMNode(this);

    const parents = $(e.target).parents().get();
    if (parents.indexOf(thisNode) >= 0) return;

    $(document).off('click', this.onDocumentClick);

    this.setState({ hasFocus: false });
  }

  onFocus() {
    this.setState({ hasFocus: true });

    $(document).on('click', this.onDocumentClick);
  }

  render() {
    const { hasFocus, query } = this.state;

    return (
      <div className="id7-search-column">
        <SearchField
          onChange={ this.onChange }
          onFocus={ this.onFocus }
          onSearch={ this.onSearch }
          ref="field"
        />
        { hasFocus ?
          <Popover
            attachTo={this.refs.field.refs.input} top={42} left={20}
            width={$(this.refs.field.refs.input).outerWidth() - 1} height={500}
          >
            <SearchView query={query} showSearchForm={false} />
          </Popover>
          : null }
      </div>
    );
  }

}
