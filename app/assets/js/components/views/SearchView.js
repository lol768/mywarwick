import React from 'react';
import Bundle from '../system/Bundle';
import once from 'lodash/once';

const Loading = () => <div />;

// Returns a Promise for the module.
// Using once() so it reuses the same value,
// though import() may mostly handle that anyway.
const importSearch = once(() => import('warwick-search-frontend'));

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
      <Bundle load={importSearch} initialise={initialiseSearch}>
        { renderSearch }
      </Bundle>
    );
  }
}
