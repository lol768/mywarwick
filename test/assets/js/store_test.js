import store from 'store';

describe('store', () => {

  it('has initial state', () => {
    store.getState().news.items.should.eql([]);
  });

  it('handles a basic action', () => {
    store.dispatch({type:'ui.class',className:'xyz'});
    store.getState().ui.className.should.equal('xyz');
  });

});