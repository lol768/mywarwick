import React from 'react';
import * as PropTypes from 'prop-types';
import TileContent from './TileContent';
import _ from 'lodash-es';

const defaultMapUrl = 'https://campus.warwick.ac.uk/?lite=1&autoLocate=true';

export default class MapTile extends TileContent {
  static propTypes = {
    params: PropTypes.object.isRequired,
  };

  static defaultProps = {
  };

  static canZoom() {
    return true;
  }

  getSmallBody() {
    return (
      <div className="reset-position">
        <img src="/assets/images/map.jpg" className="map-tile-image" alt="" />
      </div>
    );
  }

  getZoomedBody() {
    const queryParams = this.props.params.queryParams || {};
    const extraParams = _.map(queryParams, (value, key) => `&${key}=${value}`).join('');
    const iframeUrl = defaultMapUrl + extraParams;
    return (
      <div>
        <div className="tile-loading">
          <i className="fa fa-spinner fa-pulse" />
        </div>
        <iframe src={iframeUrl} frameBorder="0" title="Interactive Campus Map" />
      </div>
    );
  }

  static needsContentToRender() {
    return false;
  }

  isEmpty() {
    return false;
  }

  expandsOnClick() {
    return true;
  }
}
