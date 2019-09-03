import NewsCategoriesView from 'components/views/settings/NewsCategoriesView';
import { shallow } from 'enzyme';
import * as React from 'react';

describe('NewsCategoriesView', () => {

  const categories = [
    {id:'yellow',name:'Yellow'},
    {id:'red',name:'Red'},
    {id:'green',name:'Green'},
  ];

  const baseJsx = (<NewsCategoriesView.WrappedComponent
    dispatch={() => {}}
    categories={categories}
    subscribed={['red','green']}
    isOnline={true}
    fetching={false}
    failed={false}
  />);

  it('dispatches subscribe and unsubscribe', () => {
    const view = shallow(baseJsx);
    const instance = view.instance();
    const subscribe = sinon.spy(instance, 'subscribe');
    instance.onClick('yellow');

    subscribe.should.have.been.called;
  });

  it('dispatches subscribe and unsubscribe', () => {
    const view = shallow(baseJsx);
    const instance = view.instance();
    const unsubscribe = sinon.spy(instance, 'unsubscribe');
    instance.onClick('red');

    unsubscribe.should.have.been.called;
  });

});
