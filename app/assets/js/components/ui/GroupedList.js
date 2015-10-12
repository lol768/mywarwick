const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const _ = require('lodash');

const ListHeader = require('./ListHeader');

export default class GroupedList extends ReactComponent {

    render() {
        // Group the child nodes using the object passed to the groupBy property
        let groups = _(this.props.children)
            .groupBy(this.props.groupBy.groupForItem)
            .map((items, group) => (
                // Title the group with a list header
                <div className="list-group">
                    <ListHeader key={group + "-header"} title={this.props.groupBy.titleForGroup(group)}/>
                    {items}
                </div>
            ))
            .value();

        return (
            <div
                className={"list-group list-group--grouped list-group--grouped-" + this.props.groupBy.description}>
                {groups}
            </div>
        );
    }

}

