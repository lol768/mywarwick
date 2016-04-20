import GroupedList from 'components/ui/GroupedList';

describe('GroupedList', () => {
  let SimpleGrouping = {
    description: 'colour',
    groupForItem: sinon.spy((obj, now = "default arg") => {
      if (now !== 'default arg') {
        throw new Error("GroupedList trampled default arg")
      }
      return obj.props.paint;
    }),
    titleForGroup: sinon.spy((key) => 'Group ' + key)
  };



  it('finds the group for each item', () => {
    let groupedList = <GroupedList key="list" groupBy={SimpleGrouping}>
      <div key="a" paint="green" />
      <div key="b" paint="red" />
      <div key="c" paint="green" />
    </GroupedList>;

    shallowRender(groupedList);

    expect(SimpleGrouping.groupForItem.callCount).to.equal(3);
    expect(SimpleGrouping.titleForGroup.callCount).to.equal(2);
  });

  it('orders the groups by paint', () => {
    let groupedList = <GroupedList key="list" groupBy={SimpleGrouping}>
      <div key="c" paint="green" />
      <div key="b" paint="red" />
      <div key="a" paint="green" />
    </GroupedList>;

    let component = shallowRender(groupedList);

    expect(component.props.children.map(child => child.key)).to.eql(['group-green', 'group-red']);
  });

});
