import $ from 'jquery';
import _ from 'lodash';
import Immutable from 'immutable';

export const SEARCH_QUERY_START = 'search.query.start';
export const SEARCH_QUERY_SUCCESS = 'search.query.success';
export const SEARCH_QUERY_FAILURE = 'search.query.failure';

export const SEARCH_RESULT_CLICK = 'search.result.click';

const SITEBUILDER_GO_QUERY_URL =
  'https://sitebuilder.warwick.ac.uk/sitebuilder2/api/go/redirects.json';

function searchGoRedirects(dispatch, query) {
  dispatch({ type: SEARCH_QUERY_START, query });
  $.ajax({
    url: SITEBUILDER_GO_QUERY_URL,
    data: {
      maxResults: 5,
      prefix: query,
    },
    dataType: 'jsonp',
    success: (response) => {
      dispatch({ type: SEARCH_QUERY_SUCCESS, query, results: response });
    },
    error: () => {
      dispatch({ type: SEARCH_QUERY_FAILURE });
    },
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

const initialState = Immutable.fromJS({
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
});

const NOT_FOUND = -1;

function pushRecentItem(list, item) {
  const index = list.findIndex((i) => Immutable.fromJS(item).equals(i.get('value')));

  if (index === NOT_FOUND) {
    return list.push(Immutable.fromJS({
      count: 1,
      value: item,
    }));
  }
  return list.update(index, (recent) => recent.update('count', (count) => count + 1));
}

export function getRecentItemsOrderedByFrequency(list) {
  return Immutable.fromJS(list)
    .sort((a, b) => b.get('count') - a.get('count'))
    .map((item) => item.get('value'))
    .toJS();
}

export function reducer(state = initialState, action) {
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
}
