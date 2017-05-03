import React from 'react';
import TileContent from './TileContent';
import log from 'loglevel';

export default class MapTile extends TileContent {

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

      this.updateLocationInterval = setInterval(this.updateLocation.bind(this), 120000);
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
      const { latitude, longitude } = this.state.position;
      const width = 400;
      const height = 300;

      const src = `/service/map/${latitude.toFixed(5)}/${longitude.toFixed(5)}/${width}/${height}`;

      return <img src={src} className="map-tile-image" role="presentation" />;
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
