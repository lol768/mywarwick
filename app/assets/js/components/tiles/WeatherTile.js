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

  getLargeBody() {
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
        <Skycon className="skycon" icon={formatIconString(hour.icon)}/>
      </div>
    );
  }

  getSmallBody() {
    const hour = this.props.content.items[0];
    return (
      <div>
        <Callout {...this.props.content}/>
        <Caption {...this.props.content}/>
        <Skycon className="skycon" icon={formatIconString(hour.icon)}/>
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
    <span className="tile__text--caption">
      <div className="col-xs-3 text--light">
        Next:<br/>
        24 hrs:
      </div>
       <div className="col-xs-9">
         {nextHour.text}, in {mins} mins<br/>
         {content.daily.summary}
       </div>
    </span>
  );
};
