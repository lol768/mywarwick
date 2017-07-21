import React from 'react';
import _ from 'lodash-es';
import * as PropTypes from 'prop-types';
import TileContent, { DEFAULT_TILE_SIZES } from './TileContent';
import Skycon from '../ui/Skycon';
import { localMomentUnix } from '../../dateFormats';


function formatIconString(str) {
  return str.toUpperCase().replace(/-/g, '_');
}

const formatTime = d => localMomentUnix(d).format('ha');

export function oneWordWeather(icon) {
  return icon.replace(/.*(clear|rain|snow|sleet|wind|fog|cloudy).*/, '$1');
}

export default class WeatherTile extends TileContent {
  static canZoom() {
    return true;
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES;
  }

  getIcon() {
    const { content } = this.props;

    if (content) {
      const icon = formatIconString(content.currentConditions.icon);
      return <Skycon className="skycon" icon={icon} />;
    }

    return null;
  }

  renderIfFresh(contentFunc) {
    const { currentConditions } = this.props.content;
    if (localMomentUnix(currentConditions.time).add(20, 'minutes').isBefore()) {
      return <div>Unable to show recent weather information.</div>;
    }
    return contentFunc.call(this);
  }

  getWideBody() {
    return this.renderIfFresh(this._getWideBody);
  }

  _getWideBody() {
    const { content } = this.props;
    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-xs-5">
            <Callout temperature={content.currentConditions.temperature} />
          </div>
          <div className="col-xs-7">
            {content.minutelySummary}
          </div>
        </div>
        <WeatherTable items={content.items} />
      </div>
    );
  }

  getSmallBody() {
    return this.renderIfFresh(this._getSmallBody);
  }

  _getSmallBody() {
    const { content } = this.props;
    return (
      <div>
        <Callout temperature={content.currentConditions.temperature} />
        <div>{content.minutelySummary}</div>
      </div>
    );
  }
}

const WeatherTable = ({ items }) =>
  (<div className="row text--light">
    {_.take(items, 6).map(item => (
      <div className="col-xs-2" key={item.id}>
        <div>{formatTime(item.time)}</div>
        <div>{oneWordWeather(item.icon)}</div>
        <div>
          <i className="fa fa-tint" /> { Math.round(item.precipProbability * 100) }%
        </div>
      </div>
    ))}
  </div>);

WeatherTable.propTypes = {
  items: PropTypes.array,
};

const Callout = ({ temperature }) => (
  <span className="tile__callout">
    {Math.round(temperature)}°
  </span>
);

Callout.propTypes = {
  temperature: PropTypes.number,
};
