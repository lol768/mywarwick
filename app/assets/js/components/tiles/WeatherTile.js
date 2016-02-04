import React from 'react';

import Tile from './Tile';
import Skycon from 'react-skycons';

DEFAULT_ICON_COLOUR = 'white';

export default class WeatherTile extends Tile {

  getBody(content) {
    return (
      <div>
        <Skycon color={DEFAULT_ICON_COLOUR} icon={content.icon}/>
        <span className="caption">{content.temp}</span>
        <span className="caption">{content.caption}</span>
      </div>
    )
  }
}