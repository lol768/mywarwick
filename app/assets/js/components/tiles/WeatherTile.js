import React from 'react';

import TileContent from './TileContent';
import Skycon from '../ui/Skycon';
import moment from 'moment';
import { localMomentUnix } from '../../dateFormatter';

function formatIconString(str) {
  return str.toUpperCase().replace(/-/g, '_');
}

function formatTime(d, withContext = true) {
  const date = localMomentUnix(d);
  const now = moment();
  return (date.isSame(now, 'hour') && withContext) ? 'Now' : date.format('ha');
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
    const fiveMinsAgo = moment().subtract(5, 'minutes');
    // check against fiveMinsAgo to account for server cached data being this old
    if (localMomentUnix(nextHour.time).isBefore(fiveMinsAgo)) {
      return (
        <div>
          <Skycon className="skycon" icon={formatIconString(nextHour.icon)}/>
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
    const hour = content.items[0];
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
  <div className="row text--light">
    {content.items.map((item, i) => (
      <div className="col-xs-2" key={item.id}>
        <div>{formatTime(item.time, i === 0)}</div>
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
    <div className="tile__callout">
      <div className="col-xs-4">
        {Math.round(hour.temp)}°
      </div>
      <div className="col-xs-7">
        <small className="table-cell">{hour.text.toLowerCase()}</small>
      </div>
    </div>
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
