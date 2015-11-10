import app, { makeReducers, appendReducer, mutateReducers, unregisterAllReducers, unappendReducer, composeReducers } from 'reducers';

import Immutable from 'immutable';

describe('Reducer composition', () => {

  let add = (state = Immutable.Map({number: 0}), action) => state.update('number', (number) => number + action.number);

  it('composes a single reducer', () => {

    var reducers = makeReducers();
    reducers = appendReducer(reducers, 'counter', add);

    let fn = composeReducers(reducers.get('counter'));

    let result = fn(undefined, {type: 'add', number: 4});

    expect(result.get('number')).to.equal(4);

  });

  it('composes multiple reducers', () => {

    var reducers = makeReducers();
    reducers = appendReducer(reducers, 'counter', add);
    reducers = appendReducer(reducers, 'counter', add);

    let fn = composeReducers(reducers.get('counter'));

    let result = fn(undefined, {type: 'add', number: 4});

    expect(result.get('number')).to.equal(8);

  });

  it('runs a reducer in a subtree', () => {

    var reducers = makeReducers();
    reducers = appendReducer(reducers, 'counter', add);

    mutateReducers(reducers);

    let result = app(undefined, {type: 'counter.add', number: 4});

    expect(result.get('counter').get('number')).to.equal(4);

  });

  it('does not create empty subtrees', () => {

    mutateReducers(makeReducers());

    let initialState = app(undefined, undefined);

    let newState = app(initialState, {type: 'counter.add', number: 4});

    expect(newState).to.equal(initialState);

  });

  it('deep merges state updates', () => {

    mutateReducers(appendReducer(makeReducers(), 'test', () => Immutable.Map({
      some: {
        nested: {
          structure: true
        }
      }
    })));

    let firstState = app(undefined, {type: 'test.structure'});

    mutateReducers(appendReducer(makeReducers(), 'test', () => Immutable.Map({
      some: {
        nested: {
          structure: true,
          also: 'something else'
        }
      }
    })));

    let finalState = app(firstState, {type: 'test.structure'});

    expect(finalState.toJS().test.some.nested.also).to.equal('something else');

  });

});

