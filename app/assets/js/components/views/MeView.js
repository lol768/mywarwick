import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import tileElements from '../tiles';

import moment from 'moment';

let tileData = [
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
    callout: 3,
    text: 'actions required'
  },
  {
    key: 'live-departures',
    type: 'text',
    title: 'Live Departures',
    href: 'http://warwick.ac.uk/insite/kcm',
    backgroundColor: '#ef4050',
    icon: 'bus',
    callout: '17:52',
    text: 'U1 to Leamington'
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

  render() {
    let tiles = tileData.map((tile) => React.createElement(tileElements[tile.type], tile));

    return <div className="row">{tiles}</div>;
  }

}
