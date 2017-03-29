// FIXME won't work in Node (tests)
import warwickSearch from 'bundle-loader?lazy!warwick-search-frontend';
import React from 'react';
import Bundle from '../system/Bundle';

const Loading = () => <div />;

// Do this once after module has loaded
function initialiseSearch(mod) {
  return mod('/service/search');
}

function renderSearch(mod, SearchComponent) {
  return SearchComponent ? <SearchComponent /> : <Loading />;
}

export default class SearchView extends React.Component {
  render() {
    return (
      <Bundle load={warwickSearch} initialise={initialiseSearch}>
        { renderSearch }
      </Bundle>
    );
  }
}
