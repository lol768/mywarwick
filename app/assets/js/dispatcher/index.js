
const Rx = require('rx');
const log = require('loglevel');

/**
 * App-wide message bus. When anything needs to change, it happens
 * by pushing an action to the dispatcher. The action could be pushed
 * by an event on a UI element, or by some incoming data from the server.
 * Either way, the action is handled and processed in one place, so you
 * don't have to contend with multiple things manipulating the UI state.
 */
export class Dispatcher {

  constructor() {
    this.bus = new Rx.Subject();

    this.bus.subscribe((a) => log.debug("Dispatcher action:", a));
  }

  dispatch(action) {
    if (typeof action.type !== 'string') throw new Error("Action type must be a string:", action);
    this.bus.onNext(action);
  }

  actionsOfType(actionType: string) {
    return this.bus.filter((action) => action.type == actionType);
  }

}

export default new Dispatcher();