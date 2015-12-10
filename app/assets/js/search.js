import $ from 'jquery';

import Immutable from 'immutable';
import { registerReducer } from './reducers';

export const SEARCH_QUERY_START = 'search.query.start';
export const SEARCH_QUERY_SUCCESS = 'search.query.success';
export const SEARCH_QUERY_FAILURE = 'search.query.failure';

export const SEARCH_RESULT_CLICK = 'search.result.click';

const SITEBUILDER_GO_QUERY_URL = 'https://sitebuilder.warwick.ac.uk/sitebuilder2/api/go/redirects.json';

import _ from 'lodash';

export function fetchSearchResults(query) {
  if (_.trim(query).length == 0) {
    return {type: SEARCH_QUERY_SUCCESS, query: query, results: []};
  } else {
    return dispatch => debouncedSearchGoRedirects(dispatch, query);
  }
}

function searchGoRedirects(dispatch, query) {
  dispatch({type: SEARCH_QUERY_START, query: query});
  $.ajax({
    url: SITEBUILDER_GO_QUERY_URL,
    data: {
      maxResults: 5,
      prefix: query
    },
    dataType: 'jsonp',
    success: (response) => {
      dispatch({type: SEARCH_QUERY_SUCCESS, query: query, results: response});
    },
    error: () => {
      dispatch({type: SEARCH_QUERY_FAILURE});
    }
  });
}

let debouncedSearchGoRedirects = _.debounce(searchGoRedirects, 300);

export function clickSearchResult(result) {
  return {
    type: SEARCH_RESULT_CLICK,
    result: result
  };
}

let initialState = Immutable.fromJS({
  fetching: false,
  query: undefined,
  results: [],
  recentItems: [
    {
      count: 0,
      value: {
        path: 'webteam',
        description: 'Web Team'
      }
    }
  ]
});

const NOT_FOUND = -1;

function pushRecentItem(list, item) {
  let index = list.findIndex((i) => Immutable.fromJS(item).equals(i.get('value')));

  if (index == NOT_FOUND) {
    return list.push(Immutable.fromJS({
      count: 1,
      value: item
    }));
  } else {
    return list.update(index, (recent) => recent.update('count', (count) => count + 1));
  }
}

export function getRecentItemsOrderedByFrequency(list) {
  return Immutable.fromJS(list)
    .sort((a, b) => b.get('count') - a.get('count'))
    .map((item) => item.get('value'))
    .toJS();
}

registerReducer('search', (state = initialState, action) => {
  switch (action.type) {
    case SEARCH_RESULT_CLICK:
      return state.update('recentItems', (list) => pushRecentItem(list, action.result));
    case SEARCH_QUERY_START:
      return state.set('fetching', true).set('query', action.query);
    case SEARCH_QUERY_SUCCESS:
      return state.set('fetching', false).set('results', action.results).set('query', action.query);
    case SEARCH_QUERY_FAILURE:
      return state.set('fetching', false).set('results', []).set('query', undefined);
    default:
      return state;
  }
});