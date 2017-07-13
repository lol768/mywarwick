import store from 'store';

describe('store', () => {

  it('has initial state', () => {
    store.getState().news.items.should.eql([]);
  });

  it('handles a basic action', () => {
    store.dispatch({type:'ui.theme',theme:'xyz'});
    store.getState().ui.colourTheme.should.equal('xyz');
  });

});