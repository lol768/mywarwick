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
      .sortBy(([group, items]) => group)
      .map(([group, items]) => (
        // Title the group with a list header
        <div key={'group-' + group} className="list-group">
          <ListHeader key={'group-header-' + group} title={this.props.groupBy.titleForGroup(group)}/>
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

