import React from 'react';
import ListTile, { ListTileItem } from './ListTile';
import ShowMore from './ShowMore';
import * as PropTypes from 'prop-types';
import { formatDateTime, localMoment } from '../../dateFormats';
import { Exclaim } from '../FA';

class ModuleTileItem extends ListTileItem {
  static propTypes = {
    id: PropTypes.number.isRequired,
    moduleCode: PropTypes.string.isRequired,
    fullName: PropTypes.string.isRequired,
    href: PropTypes.string.isRequired,
    lastAccessed: PropTypes.string.isRequired,
    lastUpdated: PropTypes.string.isRequired,
  };

  render() {
    const { size, moduleCode, fullName, lastUpdated, lastAccessed } = this.props;
    if (size === 'small' || size === 'wide') {
      return (
        <li className="tile-list-item text-overflow-block">
          <span className="text--underline">{moduleCode}:</span> {fullName}
        </li>
      );
    }
    const isUpdated = localMoment(lastUpdated).isAfter(localMoment(lastAccessed));
    return (
      <li className="tile-list-item text-overflow-block">
        <span className="text--underline">{fullName}</span>
        <br />Last updated: {formatDateTime(lastUpdated)} {isUpdated && <Exclaim fw />}
        <br />Your last visit: {formatDateTime(lastAccessed)}
      </li>
    );
  }
}

export default class ModulesTile extends ListTile {
  listItem(props) {
    return <ModuleTileItem {...props} />;
  }

  getLargeBody() {
    return super.getSmallBody();
  }

  getSmallBody() {
    return (
      <div>
        {super.getSmallBody()}
        <ShowMore
          items={this.props.content.items}
          showing={super.getNumberOfItemsToDisplay()}
          onClick={this.props.onClickExpand}
        />
      </div>
    );
  }
}
