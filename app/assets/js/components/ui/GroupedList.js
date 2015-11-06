import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import _ from 'lodash';

import ListHeader from './ListHeader';

export default class GroupedList extends ReactComponent {

  render() {
    // Group the child nodes using the object passed to the groupBy property
    let groups = _(this.props.children)
      .groupBy(this.props.groupBy.groupForItem)
      .map((items, group) => (
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

