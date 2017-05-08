import React, { PropTypes } from 'react';
import TileContent from './TileContent';
import log from 'loglevel';

function isPositionOnCampus({ longitude, latitude }) {
  return latitude <= -1.548 && latitude >= -1.576
    && longitude >= 52.373 && longitude <= 52.392;
}

export default class MapTile extends TileContent {

  static propTypes = {
    imageSize: PropTypes.shape({
      width: PropTypes.number.isRequired,
      height: PropTypes.number.isRequired,
    }).isRequired,
    refreshInterval: PropTypes.number.isRequired,
  };

  static defaultProps = {
    imageSize: {
      width: 400,
      height: 300,
    },
    refreshInterval: 120000,
  };

  constructor(props) {
    super(props);

    this.state = {
      hasGeolocation: 'geolocation' in navigator,
      position: null,
    };
  }

  componentDidMount() {
    if (this.state.hasGeolocation) {
      this.updateLocation();

      const { refreshInterval } = this.props;

      this.updateLocationInterval = setInterval(this.updateLocation.bind(this), refreshInterval);
    }
  }

  componentWillUnmount() {
    clearInterval(this.updateLocationInterval);
  }

  updateLocation() {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.setState({
          position: {
            longitude: pos.coords.longitude,
            latitude: pos.coords.latitude,
          },
        });
      },
      (e) => {
        log.error('Error returned from geolocation', e);

        this.setState({
          position: null,
        });
      },
      {
        enableHighAccuracy: true,
      }
    );
  }

  static canZoom() {
    return true;
  }

  getLargeBody() {
    if (this.state.position) {
      const { imageSize: { width, height } } = this.props;
      const { position } = this.state;
      const { longitude, latitude } = position;

      const src = `/service/map/${longitude.toFixed(5)}/${latitude.toFixed(5)}/${width}/${height}`;

      if (isPositionOnCampus(position)) {
        return <img src={src} className="map-tile-image" role="presentation" />;
      }
    }

    return null;
  }

  getZoomedBody() {
    return <iframe src="https://campus.warwick.ac.uk"></iframe>;
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
