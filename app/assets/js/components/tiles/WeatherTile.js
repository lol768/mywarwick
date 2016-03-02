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

  getLargeBody(content) {
    const itemsToDisplay = this.props.zoomed ? content.items : [content.items[0]];
    return (
      <div>
        {itemsToDisplay.map(item => (
            <div key={item.id} className="tile__item">
              <span className="tile__callout">{`${Math.round(item.temp)}°`}</span>
              <Skycon className="skycon" icon={formatIconString(item.icon)}/>
              <span className="tile__text">
                {`${formatWeatherTime(item.time)}: ${item.text}`}
              </span>
            </div>
          )
        )}
      </div>
    );
  }

  getWideBody(content) {
    return this.getLargeBody(content);
  }

  getSmallBody(content) {
    return this.getLargeBody(content);
  }
}
