import React from 'react';

import Popover from './Popover';
import { SearchView } from '../views/SearchView';

import { connect } from 'react-redux';

import $ from 'jquery';

class MastheadSearch extends SearchView {

  render() {
    return (
      <div className="id7-search-column">
        { this.searchField() }
        { this.state.searchFocus ?
          <Popover
            attachTo={this.refs.field.refs.input} top={42} left={20}
            width={$(this.refs.field.refs.input).outerWidth() - 1} height={300}
          >
            {this.suggestions()}
          </Popover>
          : null}
      </div>
    );
  }

}

const select = (state) => state.search;

export default connect(select)(MastheadSearch);
