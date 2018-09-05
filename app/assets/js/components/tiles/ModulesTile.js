import React from 'react';
import ListTile, { ListTileItem } from './ListTile';
import ShowMore from './ShowMore';
import * as PropTypes from 'prop-types';
import { formatDateTime } from '../../dateFormats';
import HyperLink from '../ui/Hyperlink';
import DismissableInfoModal from '../ui/DismissableInfoModal';
// import { Info } from '../FA';

class ModuleAnnouncement extends React.PureComponent {
  static propTypes = {
    name: PropTypes.string.isRequired,
    url: PropTypes.string.isRequired,
    date: PropTypes.string.isRequired,
  };

  render() {
    return (
      <li className="tile-list-item text-overflow-block">
        {formatDateTime(this.props.date)}:&nbsp;
        <HyperLink
          className="text--dotted-underline"
          href={this.props.url}
        >
          {this.props.name}
        </HyperLink>
      </li>
    );
  }
}

class ModuleTileItem extends ListTileItem {
  static propTypes = {
    id: PropTypes.number.isRequired,
    moduleCode: PropTypes.string,
    fullName: PropTypes.string.isRequired,
    href: PropTypes.string.isRequired,
    lastUpdated: PropTypes.string.isRequired,
    announcements: PropTypes.arrayOf(PropTypes.shape(ModuleAnnouncement.propTypes)),
    showModal: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    const { fullName, announcements, moduleCode, showModal, hideModal, lastUpdated } = this.props;
    const modalMoreBtn =
      (<HyperLink type="button" className="btn btn-default" href={this.props.href}>
        Open module in Moodle
      </HyperLink>);
    const modal =
      (<DismissableInfoModal
        heading={moduleCode ? `${moduleCode}: ${fullName}` : fullName}
        subHeadings={[`Last updated: ${formatDateTime(lastUpdated)}`]}
        onDismiss={hideModal}
        moreButton={modalMoreBtn}
      >
        <h6>Announcements</h6>
        <ul>
          {announcements.map((props, i) => <ModuleAnnouncement {...props} key={i} />)}
        </ul>
      </DismissableInfoModal>);
    showModal(modal);
  }

  render() {
    const { size, moduleCode, fullName, href } = this.props;
    // const { lastUpdated, announcements} = this.props;
    const isSmall = size === 'small' || size === 'wide';
    const content = isSmall ?
      (<span><span className="text--underline">{moduleCode ? `${moduleCode}:` : fullName}</span>
        &nbsp;{moduleCode && fullName}</span>)
      :
      (<span className="text--underline">{moduleCode && `${moduleCode}: `}{fullName}</span>);

    return (
      <li className="tile-list-item--big">
        {/* {announcements.length ?*/}
        {/* <a onClick={this.handleClick} role="button" target="_blank" tabIndex={0}>*/}
        {/* {content}*/}
        {/* <Info fw />*/}
        {/* </a>*/}
        {/* :*/}
        <HyperLink href={href}>
          {content}
        </HyperLink>
        {/* }*/}
        {/* {!isSmall && <div>Last updated: {formatDateTime(lastUpdated)}</div>}*/}
      </li>
    );
  }
}

export default class ModulesTile extends ListTile {
  hideModal() {
    super.hideModal();
  }

  listItem(props) {
    return (<ModuleTileItem
      {...props}
      showModal={this.props.showModal}
      hideModal={this.hideModal}
    />);
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
