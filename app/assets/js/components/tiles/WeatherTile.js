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

  componentWillReceiveProps(nextProps) {
    const { content, setIcon } = nextProps;
    // if content has just arrived in props received
    if (!this.props.content && content) {
      const icon = formatIconString(content.items[0].icon);
      return setIcon(<Skycon className="skycon" icon={icon}/>);
    }
  }

  renderIfFresh(contentFunc) {
    const { content } = this.props;
    const nextHour = content.items[1];
    if (localMomentUnix(nextHour.time).isBefore()) {
      return (
        <div>
          <div>Unable to show recent weather information.</div>
        </div>
      );
    }
    return contentFunc.call(this);
  }

  getLargeBody() {
    return this.renderIfFresh(this._getLargeBody);
  }

  _getLargeBody() {
    const { content } = this.props;
    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-xs-5">
            <Callout {...content}/>
          </div>
          <div className="col-xs-7">
            <Caption {...content}/>
          </div>
        </div>
        <WeatherTable items={content.items}/>
      </div>
    );
  }

  getSmallBody() {
    return this.renderIfFresh(this._getSmallBody);
  }

  _getSmallBody() {
    return (
      <div>
        <Callout {...this.props.content}/>
        <Caption {...this.props.content}/>
      </div>
    );
  }
}

const WeatherTable = (content) =>
  <div className="row text--light">
    {content.items.map(item => (
      <div className="col-xs-2" key={item.id}>
        <div>{formatWeatherTime(item.time)}</div>
        <div>{WeatherTile.oneWordWeather(item.icon)}</div>
        <div>
          <i className="fa fa-tint"/> {Math.round(item.precipProbability * 100)}%
        </div>
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
    <div>
      <div>{nextHour.text}, in {plural(mins, 'min')}</div>
      <div>{content.daily.summary}</div>
    </div>
  );
};
