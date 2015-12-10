import GroupedList from 'components/ui/GroupedList';

describe('GroupedList', () => {
  let SimpleGrouping = {
    description: 'individual',
    groupForItem: sinon.spy((props) => props.key),
    titleForGroup: sinon.spy((key) => 'Group ' + key)
  };

  it('finds the group for each item', () => {
    let groupedList = <GroupedList key="list" groupBy={SimpleGrouping}>
      <div key="a"></div>
      <div key="b"></div>
      <div key="c"></div>
    </GroupedList>;

    shallowRender(groupedList);

    expect(SimpleGrouping.groupForItem.callCount).to.equal(3);
    expect(SimpleGrouping.titleForGroup.callCount).to.equal(3);
  });

  it('orders the groups by key', () => {
    let groupedList = <GroupedList key="list" groupBy={SimpleGrouping}>
      <div key="c"></div>
      <div key="b"></div>
      <div key="a"></div>
    </GroupedList>;

    let component = shallowRender(groupedList);

    expect(component.props.children.map(child => child.key)).to.eql(['group-a', 'group-b', 'group-c']);
  });

});
