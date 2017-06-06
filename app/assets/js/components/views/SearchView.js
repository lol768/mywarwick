import React, { PropTypes } from 'react';
import Bundle from '../system/Bundle';
import once from 'lodash-es/once';
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';

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

export default class SearchView extends React.PureComponent {

  static propTypes = {};

  render() {
    return (
      <ScrollRestore url={`/${Routes.SEARCH}`}>
        <Bundle load={importSearch} initialise={initialiseSearch}>
          { renderSearch }
        </Bundle>
      </ScrollRestore>
    );
  }
}
