import React from 'react';
import ListTile, { ListTileItem } from './ListTile';
import ShowMore from './ShowMore';
import * as PropTypes from 'prop-types';
import { formatDateTime } from '../../dateFormats';
import HyperLink from '../ui/Hyperlink';

class ModuleTileItem extends ListTileItem {
  static propTypes = {
    id: PropTypes.number.isRequired,
    moduleCode: PropTypes.string,
    fullName: PropTypes.string.isRequired,
    href: PropTypes.string.isRequired,
    lastUpdated: PropTypes.string.isRequired,
  };

  render() {
    const { size, moduleCode, fullName, lastUpdated, href } = this.props;
    if (size === 'small' || size === 'wide') {
      return (
        <li className="tile-list-item text-overflow-block">
          <HyperLink href={href}>
            <span className="text--underline">{moduleCode ? `${moduleCode}:` : fullName}</span>
            &nbsp;{moduleCode && fullName}
          </HyperLink>
        </li>
      );
    }
    return (
      <li className="tile-list-item text-overflow-block">
        <HyperLink href={href}>
          <span className="text--underline">{moduleCode && `${moduleCode}: `}{fullName}</span>
        </HyperLink>
        <div>Last updated: {formatDateTime(lastUpdated)}</div>
      </li>
    );
  }
}

export default class ModulesTile extends ListTile {
  listItem(props) {
    return <ModuleTileItem {...props} />;
  }

  getLargeBody() {
    return (
      <div>
        {super.getSmallBody()}
        {!this.props.zoomed &&
        <ShowMore
          items={this.props.content.items}
          showing={super.getNumberOfItemsToDisplay()}
          onClick={this.props.onClickExpand}
        />}
      </div>
    );
  }

  getSmallBody() {
    return this.getLargeBody();
  }
}
