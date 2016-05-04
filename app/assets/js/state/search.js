import $ from 'jquery';
import _ from 'lodash';

export const SEARCH_QUERY_START = 'search.query.start';
export const SEARCH_QUERY_SUCCESS = 'search.query.success';
export const SEARCH_QUERY_FAILURE = 'search.query.failure';

export const SEARCH_RESULT_CLICK = 'search.result.click';

const SITEBUILDER_GO_QUERY_URL =
  'https://sitebuilder.warwick.ac.uk/sitebuilder2/api/go/redirects.json';

function searchGoRedirects(dispatch, query) {
  dispatch({ type: SEARCH_QUERY_START, query });
  // don't really use the result of dispatching this, but I've made it
  // return a promise anyway.
  return new Promise((res, rej) => {
    // using jQuery as fetch() won't do jsonp for us. This
    // is going away soon so don't really care.
    $.ajax({
      url: SITEBUILDER_GO_QUERY_URL,
      data: {
        maxResults: 5,
        prefix: query,
      },
      dataType: 'jsonp',
      success: (response) => {
        dispatch({ type: SEARCH_QUERY_SUCCESS, query, results: response });
        res();
      },
      error: (e) => {
        dispatch({ type: SEARCH_QUERY_FAILURE });
        rej(e);
      },
    });
  });
}

const debouncedSearchGoRedirects = _.debounce(searchGoRedirects, 300);

export function fetchSearchResults(query) {
  if (_.trim(query).length === 0) {
    return { type: SEARCH_QUERY_SUCCESS, query, results: [] };
  }
  return dispatch => debouncedSearchGoRedirects(dispatch, query);
}

export function clickSearchResult(result) {
  return {
    type: SEARCH_RESULT_CLICK,
    result,
  };
}

const initialState = {
  fetching: false,
  query: undefined,
  results: [],
  recentItems: [
    {
      count: 0,
      value: {
        path: 'webteam',
        description: 'Web Team',
      },
    },
  ],
};

const NOT_FOUND = -1;

function pushRecentItem(list, item) {
  const index = list.findIndex((i) => _.isEqual(item, i.value));

  if (index === NOT_FOUND) {
    return list.concat({
      count: 1,
      value: item,
    });
  }

  // Existing item - replace with incremented hit count
  const existing = list[index];
  const replacement = {
    ...existing,
    count: existing.count + 1,
  };
  return [].concat(list.slice(0, index), replacement, list.slice(index + 1));
}

export function getRecentItemsOrderedByFrequency(list) {
  return _(list).sortBy('count').map('value').value();
}

export function reducer(state = initialState, action) {
  switch (action.type) {
    case SEARCH_RESULT_CLICK:
      return {
        ...state,
        recentItems: pushRecentItem(state.recentItems, action.result),
      };
    case SEARCH_QUERY_START:
      return {
        ...state,
        fetching: true,
        query: action.query,
      };
    case SEARCH_QUERY_SUCCESS:
      return {
        ...state,
        fetching: false,
        results: action.results,
        query: action.query,
      };
    case SEARCH_QUERY_FAILURE:
      return {
        ...state,
        fetching: false,
        results: [],
        query: undefined,
      };
    default:
      return state;
  }
}
