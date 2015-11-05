import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import * as tileElements from '../tiles';

import moment from 'moment';
import _ from 'lodash';

let TILE_DATA = [
  {
    key: 'mail',
    type: 'list',
    title: 'Mail',
    href: 'http://warwick.ac.uk/mymail',
    backgroundColor: '#0078d7',
    icon: 'envelope-o',
    word: 'unread',
    size: 'wide',
    items: [
      {
        key: 1,
        title: 'Christelle Evaert',
        text: 'Departmental meeting cancelled',
        date: moment().subtract(1, 'hour').toISOString()
      },
      {
        key: 2,
        title: 'IT Service Desk',
        text: 'Emergency RFC',
        date: moment().subtract(4, 'hour').toISOString()
      },
      {
        key: 3,
        title: 'Linda Squirrel',
        text: 'IT Induction Day reminder',
        date: moment().subtract(26, 'hour').toISOString()
      }
    ]
  },
  {
    key: 'tabula',
    type: 'text',
    title: 'Tabula',
    href: 'https://tabula.warwick.ac.uk',
    backgroundColor: '#239b92',
    icon: 'cog',
    items: [
      {
        key: 1,
        callout: 3,
        text: 'actions required'
      }
    ]
  },
  {
    key: 'live-departures',
    type: 'text',
    title: 'Live Departures',
    backgroundColor: '#ef4050',
    icon: 'bus',
    items: [
      {
        key: 1,
        callout: '17:52',
        text: 'U1 to Leamington'
      },
      {
        key: 2,
        callout: '18:01',
        text: '11 to Coventry'
      }
    ]
  },
  {
    key: 'modules',
    type: 'list',
    title: 'My Modules',
    icon: 'mortar-board',
    items: [
      {
        key: 1,
        href: 'http://warwick.ac.uk/cs118',
        text: 'CS118 Programming for Computer Scientists'
      },
      {
        key: 2,
        href: 'http://warwick.ac.uk/cs256',
        text: 'CS256 Functional Programming'
      },
      {
        key: 3,
        href: 'http://warwick.ac.uk/cs324',
        text: 'CS324 Computer Graphics'
      }
    ]
  }
];

export default class MeView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      zoomedTile: null
    };
  }

  onTileClick(tile) {
    if (tile.href) {
      window.open(tile.href);
    } else {
      this.setState({
        zoomedTile: this.state.zoomedTile ? null : tile
      });
    }
  }

  renderTile(tile) {
    let onTileClick = this.onTileClick.bind(this);

    let element = tileElements[tile.type];

    let props = _.merge(tile, {
      onClick(e) {
        onTileClick(tile, e);
      },

      zoomed: this.state.zoomedTile == tile
    });

    return React.createElement(element, props);
  }

  renderTiles() {
    let { zoomedTile } = this.state;

    if (zoomedTile) {
      return this.renderTile(zoomedTile);
    } else {
      return TILE_DATA.map((tile) => this.renderTile(tile));
    }
  }

  render() {
    return <div className="row">{this.renderTiles()}</div>;
  }

}
