
const _ = require('lodash');

/**
 * Helper function for creating an action object to
 * be dispatcher by the dispatcher.
 */
export function newAction(type, rest) {
  return _.merge({type: type}, rest);
}


