import React from 'react';

import TileContent from './TileContent';
import Skycon from '../ui/Skycon';
import moment from 'moment';
import { localMomentUnix } from '../../dateFormatter';

function formatIconString(str) {
  return str.toUpperCase().replace(/-/g, '_');
}

function formatWeatherTime(d) {
  const date = localMomentUnix(d);
  const now = moment();
  return date.isSame(now, 'hour') ? 'Now' : date.format('ha');
}

export default class WeatherTile extends TileContent {

  static canZoom() {
    return true;
  }

  static oneWordWeather(icon) {
    return icon.replace(/.*(clear|rain|snow|sleet|wind|fog|cloudy).*/, '$1');
  }

  renderIfFresh(contentFunc) {
    const nextHour = this.props.content.items[1];
    if (localMomentUnix(nextHour.time).isSameOrBefore()) {
      return <div>Unable to show recent weather information.</div>;
    }
    return contentFunc.call(this);
  }

  getLargeBody() {
    return this.renderIfFresh(this._getLargeBody);
  }

  _getLargeBody() {
    const { content } = this.props;
    const hour = content.items[0];
    return (
      <div>
        <div className="col-xs-5">
          <Callout {...content}/>
        </div>
        <div className="col-xs-7">
          <Caption {...content}/>
        </div>
        <WeatherTable items={content.items}/>
        {!this.props.errors ?
          <Skycon className="skycon" icon={formatIconString(hour.icon)}/>
          : null
        }
      </div>
    );
  }

  getSmallBody() {
    return this.renderIfFresh(this._getSmallBody);
  }

  _getSmallBody() {
    const hour = this.props.content.items[0];
    return (
      <div>
        <Callout {...this.props.content}/>
        <Caption {...this.props.content}/>
        {!this.props.errors ?
          <Skycon className="skycon" icon={formatIconString(hour.icon)}/>
          : null
        }
      </div>
    );
  }
}

const WeatherTable = (content) =>
  <div>
    {content.items.map(item => (
      <div className="col-xs-2 table__item text--light" key={item.id}>
        <span className="tile__text">{formatWeatherTime(item.time)}</span>
        <span className="tile__text">{WeatherTile.oneWordWeather(item.icon)}</span>
        <span className="tile__text">
          <i className="fa fa-tint"/> {Math.round(item.precipProbability * 100)}%
        </span>
      </div>
    ))}
  </div>;

function plural(i, one, many = `${one}s`) {
  const word = i === 1 ? one : many;
  return `${i} ${word}`;
}

const Callout = (content) => {
  const hour = content.items[0];
  return (
    <span className="tile__callout">{Math.round(hour.temp)}°
      <small> {hour.text.toLowerCase()}</small>
    </span>
  );
};

const Caption = (content) => {
  const nextHour = content.items[1];
  const mins = localMomentUnix(nextHour.time).diff(moment(), 'minutes');
  return (
    <div className="tile__text--caption">
      <div>{nextHour.text}, in {plural(mins, 'min')}</div>
      <div>{content.daily.summary}</div>
    </div>
  );
};
