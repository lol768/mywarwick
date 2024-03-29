import React from 'react';
import once from 'lodash-es/once';
import Bundle from '../system/Bundle';
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import log from 'loglevel';

const Loading = () => <div />;

// Returns a Promise for the module.
// Using once() so it reuses the same value,
// though import() may mostly handle that anyway.
const importSearch = once(() => import(/* webpackChunkName: "search-import" */'warwick-search-frontend').catch((e) => {
  log.error(e);
  throw e;
}));

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
