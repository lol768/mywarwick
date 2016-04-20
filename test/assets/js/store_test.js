import Immutable from 'immutable';
import store from 'store';

describe('store', () => {

  it('has initial state', () => {
    store.getState().getIn(['routing','location']).should.equal(Immutable.Map());
  });

  it('handles a basic action', () => {
    store.dispatch({type:'ui.class',className:'xyz'});
    store.getState().getIn(['ui','className']).should.equal('xyz');
  });

});