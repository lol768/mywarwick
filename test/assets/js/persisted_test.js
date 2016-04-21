import persistedLib from 'persisted';
import * as redux from 'redux';
import Immutable from 'immutable';
import MemoryLocalForage from './testhelpers/MemoryLocalForage';

describe('persisted', () => {

  const initialState = Immutable.fromJS({actions:[]});
  const reducer = function (state=initialState, action) {
    switch (action.type) {
      case 'test.wipe': return initialState;
      case 'test.store': return state.merge({
        subsection: {
          storedvalue: action.payload
        }
      });
      // other actions: just store them in a list
      default: return state.merge({
        actions: state.get('actions').push(action),
      });
    }
  };
  const store = redux.createStore(reducer);
  const localforage = new MemoryLocalForage();
  const persisted = persistedLib({ store, localforage });

  spy(store, 'subscribe');

  const saveItem = (val) => ({
    type: 'test.store',
    payload: val,
  });

  const nothingUseful = (val) => ({
    type: 'nothingUseful',
    payload: val
  })

  beforeEach(() => {
    store.dispatch({type: 'test.wipe'});
    return localforage.clear();
  })

  it('thaws a value into store using defaults', () => {
    return localforage.setItem('testkey', 'testval')
      .then(() => persisted('testkey', nothingUseful))
      .then(() => {
        assert(store.subscribe.called);
        const actions = store.getState().get('actions').toArray();
        actions.should.include({type:'nothingUseful',payload:'testval'});
      });
  });

  it('doesn\'t dispatch if local storage is empty', () => {
    return persisted('testkey', nothingUseful)
      .then(() => {
        assert(store.subscribe.called);
      });
  });

  it('freezes items from nested redux key into flat localforage key', () => {
    return persisted('subsection.storedvalue', nothingUseful)
      .then(() => store.dispatch(saveItem('cool value')))
      .then(() => localforage.getItem('subsection.storedvalue'))
      .then((val) => {
        // it's been saved into local storage
        val.should.equal('cool value');
      })
  })

});