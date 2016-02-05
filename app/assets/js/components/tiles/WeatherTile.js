import React from 'react';

import Tile from './Tile';
import Skycons from 'react-skycons';
import moment from 'moment';

const DEFAULT_ICON_COLOUR = 'white';

function formatIconString(str) {
  return str.toUpperCase().replace(/-/g, '_');
}

function formatWeatherTime(d) {
  const date = moment.unix(d);
  const now = moment();
  return date.isSame(now, 'hour') ? 'Now' : date.format('ha');
}

export default class WeatherTile extends Tile {
  canZoom() {
    return true;
  }

  getBody(content) {
    const itemsToDisplay = this.isZoomed() ? content.items : [content.items[0]];
    return (
      <div>
        {itemsToDisplay.map(item => (
            <div key={item.id} className="tile__item">
              <span className="tile__callout">{`${Math.ceil(item.temp)}°C`}</span>
              <Skycons className="skycon" color={DEFAULT_ICON_COLOUR}
                icon={formatIconString(item.icon)}
              />
              <span className="tile__text">
                {`${formatWeatherTime(item.time)}: ${item.text}`}
              </span>
            </div>
          )
        )}
      </div>
    );
  }
}
