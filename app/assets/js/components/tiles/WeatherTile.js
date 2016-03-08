import React from 'react';

import TileContent from './TileContent';
import Skycon from '../ui/Skycon';
import moment from 'moment';

function formatIconString(str) {
  return str.toUpperCase().replace(/-/g, '_');
}

function formatWeatherTime(d) {
  const date = moment.unix(d);
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

  mapToTableRow(itemsToDisplay) {
    return itemsToDisplay.map(item => (
      <div className="col-xs-2 table__item text--light" key={item.id}>
        <span className="tile__text">{formatWeatherTime(item.time)}</span>
        <span className="tile__text">{WeatherTile.oneWordWeather(item.icon)}</span>
        <span className="tile__text">
          <i className="fa fa-tint"/> {item.precipProbability * 100}%
        </span>
      </div>
    ));
  }

  getCallout() {
    const { content } = this.props;
    const hour = content.items[0];
    return (
      <span className="tile__callout">{`${Math.round(hour.temp)}°`}
        <small> {hour.text.toLowerCase()}</small>
      </span>
    );
  }

  getCaption() {
    const { content } = this.props;
    const nextHour = content.items[1];
    const mins = moment.unix(nextHour.time).diff(moment(), 'minutes');
    return (
      <span className="tile__text--caption">
            <span className="text--light">Next:</span> {nextHour.text}, in {mins} mins
            <br/>
            <span className="text--light">24 hrs:</span> {content.daily.summary}
      </span>
    );
  }

  getLargeBody() {
    const { content } = this.props;
    const hour = content.items[0];
    return (
      <div>
        <div className="col-xs-5">{this.getCallout()}</div>
        <div className="col-xs-7">{this.getCaption()}</div>
        {this.mapToTableRow(content.items)}
        <Skycon className="skycon" icon={formatIconString(hour.icon)}/>
      </div>
    );
  }

  getSmallBody() {
    const hour = this.props.content.items[0];
    return (
      <div>
        {this.getCallout()}
        {this.getCaption()}
        <Skycon className="skycon" icon={formatIconString(hour.icon)}/>
      </div>
    );
  }
}
