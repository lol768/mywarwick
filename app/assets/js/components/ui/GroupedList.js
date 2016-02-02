import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import _ from 'lodash';

import ListHeader from './ListHeader';

export default class GroupedList extends ReactComponent {

  render() {
    if (this.props.groupBy === undefined) {
      return (
        <div className="list-group">
          {this.props.children}
        </div>
      );
    }

    // Group the child nodes using the object passed to the groupBy property
    let groups = _(this.props.children)
    // don't pass directly to groupBy - tramples on your default args.
      .groupBy((obj) => this.props.groupBy.groupForItem(obj))
      .pairs()
      .sortBy(([group, item]) => group); // eslint-disable-line no-unused-vars

    if (this.props.orderDescending) {
      groups = groups.reverse();
    }

    const orderedGroups = groups.map(([group, items]) => (
        // Title the group with a list header
        <div key={`group-${group}`} className="list-group">
          <ListHeader
            key={`group-header-${group}`}
            title={this.props.groupBy.titleForGroup(group)}
          />
          {items}
        </div>
      ))
      .value();

    return (
      <div
        className={
          `list-group list-group--grouped list-group--grouped-${this.props.groupBy.description}`
        }
      >
        {orderedGroups}
      </div>
    );
  }

}
