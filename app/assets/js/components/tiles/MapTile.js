import React from 'react';
import * as PropTypes from 'prop-types';
import TileContent from './TileContent';
import _ from 'lodash-es';

const defaultMapUrl = 'https://campus.warwick.ac.uk/?lite=1&autoLocate=true';

export default class MapTile extends TileContent {
  static propTypes = {
    params: PropTypes.object,
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
    const queryParams = (this.props.params || {}).queryParams || {};
    const extraParams = _.map(queryParams, (value, key) => `&${key}=${value}`).join('');
    const iframeUrl = defaultMapUrl + extraParams;
    const iframeHTML = `<iframe src=${iframeUrl} frameBorder="0" title="Interactive Campus Map" allow="geolocation" />`;
    return (
      <div>
        <div className="tile-loading">
          <i className="fa fa-spinner fa-pulse" />
        </div>
        <div
          // eslint-disable-next-line react/no-danger
          dangerouslySetInnerHTML={{ __html: iframeHTML }}
        />
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
