
const a = require('../../actions');

// TODO maybe actions could be Rx subjects, all piping into dispatcher.
export let actions = {
  SEARCH: 'people.search',
  search: (name) => a.newAction(actions.SEARCH, {name: name}),

  SEARCH_RESULT: 'people.searchResult',
  searchResult: (name, results) => a.newAction(actions.SEARCH_RESULT, {name, results})
};

export let PeopleTile = require('./peopletile');