import React from 'react';
import * as PropTypes from 'prop-types';

import _ from 'lodash-es';

import ListHeader from './ListHeader';

export default class GroupedList extends React.PureComponent {
  static propTypes = {
    groupBy: PropTypes.shape({
      getGroupedItems: PropTypes.func,
      groupForItem: PropTypes.func,
      titleForGroup: PropTypes.func.isRequired,
      subtitleForGroup: PropTypes.func,
      noRepeatSubtitle: PropTypes.bool,
    }),
    children: PropTypes.node.isRequired,
    className: PropTypes.string,
    orderDescending: PropTypes.bool,
  };

  getGroupedItems() {
    const { groupBy, children } = this.props;

    if ('getGroupedItems' in groupBy) {
      return groupBy.getGroupedItems(children);
    }

    return _.flow(
      _.partialRight(_.groupBy, obj => this.props.groupBy.groupForItem(obj)),
      _.toPairs,
      _.partialRight(_.sortBy, ([group]) => group),
    )(this.props.children);
  }

  render() {
    if (this.props.groupBy === undefined) {
      return (
        <div className={ this.props.className }>
          {this.props.children}
        </div>
      );
    }

    // Group the child nodes using the object passed to the groupBy property
    let groups = this.getGroupedItems();

    if (this.props.orderDescending) {
      groups = groups.reverse();
    }

    let subtitle;
    const getSubtitle = (items) => {
      const oldSub = subtitle;
      subtitle = this.props.groupBy.subtitleForGroup ?
        this.props.groupBy.subtitleForGroup(items) : null;
      if (this.props.groupBy.noRepeatSubtitle) {
        return subtitle !== oldSub ? subtitle : null;
      }
      return subtitle;
    };

    const orderedGroups = groups.map(([group, items]) => (
      // Title the group with a list header
      <div key={`group-${group}`} className="list-group">
        <ListHeader
          key={`group-header-${group}`}
          title={this.props.groupBy.titleForGroup(group)}
          subtitle={getSubtitle(items)}
        />
        {items}
      </div>
    ));

    return (
      <div className={ this.props.className }>
        {orderedGroups}
      </div>
    );
  }
}
