import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import moment from 'moment';
import $ from 'jquery';

import TileContent, { TILE_SIZES } from './TileContent';
import Hyperlink from '../ui/Hyperlink';
import * as FA from '../FA';
import wrapKeyboardSelect from '../../keyboard-nav';
import ShowMore from './ShowMore';
import DismissableInfoModal from '../ui/DismissableInfoModal';

export default class EAPTile extends TileContent {
  static propTypes = {
    content: PropTypes.shape({}),
  };

  static canZoom() {
    return true;
  }

  static supportedTileSizes() {
    return [TILE_SIZES.WIDE];
  }

  static isRemovable() {
    return false;
  }

  static isMovable() {
    return false;
  }

  static renderDateAndName(item) {
    const parseFormat = 'ddd D MMM YYYY';
    const printFormat = 'do MMMM';

    if (!item.startDate && !item.endDate) {
      return item.name;
    }

    if (!item.endDate) {
      const startDate = moment(item.startDate, parseFormat).format(printFormat);
      return `From ${startDate}, ${item.name}`;
    }

    if (!item.startDate) {
      const endDate = moment(item.endDate, parseFormat).format(printFormat);
      return `Until ${endDate}, ${item.name}`;
    }

    const startDate = moment(item.startDate, parseFormat).format(printFormat);
    const endDate = moment(item.endDate, parseFormat).format(printFormat);
    return `${startDate} - ${endDate}, ${item.name}`;
  }

  static modalMoreButton(item) {
    return (
      <a
        type="button"
        className="btn btn-default"
        target="_blank"
        rel="noopener noreferrer"
        href={item.feedbackUrl}
      >
        Send feedback
      </a>
    );
  }

  static modalSubheading(item) {
    if (!item.startDate && !item.endDate) {
      return null;
    }

    if (!item.endDate) {
      return (
        <span>
          <FA.Calendar />
          {' '}
          Evaluation period for this feature is from {item.startDate}
        </span>
      );
    }

    if (!item.startDate) {
      return (
        <span>
          <FA.Calendar />
          {' '}
          Evaluation period for this feature is until {item.endDate}
        </span>
      );
    }

    return (
      <span>
        <FA.Calendar />
        {' '}
        Evaluation period for this feature is {item.startDate} until {item.endDate}
      </span>
    );
  }

  constructor(props) {
    super(props);

    this.onMoreInfo = this.onMoreInfo.bind(this);
    this.showModal = this.showModal.bind(this);
    this.hideModal = this.hideModal.bind(this);
  }

  onMoreInfo(e) {
    wrapKeyboardSelect(() => {
      const id = $(e.target).closest('.feature').data('id');
      const item = _.find(this.props.content.items, i => i.id === id);
      if (item) {
        this.showModal(item);
      }
    }, e);
  }

  hideModal() {
    this.props.showModal(null);
  }

  showModal(item) {
    const modal = (<DismissableInfoModal
      heading={item.name}
      subHeadings={[EAPTile.modalSubheading(item)]}
      onDismiss={this.hideModal}
      moreButton={item.feedbackUrl && EAPTile.modalMoreButton(item)}
    >
      <div
        // eslint-disable-next-line react/no-danger
        dangerouslySetInnerHTML={{ __html: item.summary }}
      />
    </DismissableInfoModal>);
    this.props.showModal(modal);
  }

  renderItem(item) {
    return (
      <li key={item.id} className="feature" data-id={item.id}>
        <div className="date-and-name">{EAPTile.renderDateAndName(item)}</div>
        {item.summary && item.summary.length > 0 && item.summaryRaw && item.summaryRaw.length > 0
          && <div
            className="more-info"
            role="button"
            tabIndex={0}
            onClick={ this.onMoreInfo }
            onKeyUp={ this.onMoreInfo }
          >
            <FA.Info />More info
          </div>
        }
        {item.feedbackUrl && item.feedbackUrl.length > 0
          && <div className="feedback">
            <Hyperlink href={item.feedbackUrl}>
              <FA.Commenting />Send feedback
            </Hyperlink>
          </div>
        }
      </li>
    );
  }

  getSmallBody() {
    const { items } = this.props.content;
    const itemsToShow = (this.props.zoomed) ? items : _.take(items, 3);
    return (
      <div>
        <ul className="list-unstyled">
          {_.map(itemsToShow, item => this.renderItem(item))}
        </ul>
        {!this.props.zoomed
          && <ShowMore
            items={this.props.content.items}
            showing={3}
            onClick={this.props.onClickExpand}
          />
        }
      </div>
    );
  }
}
