import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import _ from 'lodash';

import ListHeader from './ListHeader';

export default class GroupedList extends ReactComponent {

  getGroupedItems() {
    const { groupBy, children } = this.props;

    if ('getGroupedItems' in groupBy) {
      return groupBy.getGroupedItems(children);
    }

    return _(this.props.children).chain()
      .groupBy((obj) => this.props.groupBy.groupForItem(obj))
      .pairs()
      .sortBy(([group]) => group)
      .value();
  }

  render() {
    if (this.props.groupBy === undefined) {
      return (
        <div className="list-group">
          {this.props.children}
        </div>
      );
    }

    // Group the child nodes using the object passed to the groupBy property
    let groups = this.getGroupedItems();

    if (this.props.orderDescending) {
      groups = groups.reverse();
    }

    const orderedGroups = groups.map(([group, items]) => (
      // Title the group with a list header
      <div key={`group-${group}`} className="tile-list-group">
        <ListHeader
          key={`group-header-${group}`}
          title={this.props.groupBy.titleForGroup(group)}
        />
        {items}
      </div>
    ));

    return (
      <div>
        {orderedGroups}
      </div>
    );
  }

}
