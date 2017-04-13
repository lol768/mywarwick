import NewsCategoriesView from 'components/views/NewsCategoriesView';
import { shallow } from 'enzyme';

describe('NewsCategoriesView', () => {

  const items = [
    {id:'yellow',name:'Yellow'},
    {id:'red',name:'Red'},
    {id:'green',name:'Green'},
  ];

  const baseJsx = (<NewsCategoriesView
    dispatch={() => {}}
    items={items}
    subscribed={['red','green']}
  />);

  it('dispatches subscribe and unsubscribe', () => {
    const view = shallow(baseJsx);
    const instance = view.instance();
    const subscribe = sinon.spy(instance, 'subscribe');
    instance.onChange([{value: 'yellow'}]);

    subscribe.should.have.been.called;
  });

  it('dispatches subscribe and unsubscribe', () => {
    const view = shallow(baseJsx);
    const instance = view.instance();
    const unsubscribe = sinon.spy(instance, 'unsubscribe');
    instance.onChange([{value: 'red'}]);

    unsubscribe.should.have.been.called;
  });

});
