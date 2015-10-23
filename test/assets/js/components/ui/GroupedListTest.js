import GroupedList from 'components/ui/GroupedList';
import ListHeader from 'components/ui/ListHeader';

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

});
