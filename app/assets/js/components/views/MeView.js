import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import { ListTile, CountTile } from '../tiles';

import moment from 'moment';

let mail = {
  title: 'Mail',
  href: 'http://warwick.ac.uk/mymail',
  backgroundColor: '#0078d7',
  icon: 'envelope-o',
  word: 'unread',
  items: [
    {
      title: 'Christelle Evaert',
      text: 'Departmental meeting cancelled',
      date: moment().subtract(1, 'hour').toISOString()
    },
    {
      title: 'IT Service Desk',
      text: 'Emergency RFC',
      date: moment().subtract(4, 'hour').toISOString()
    },
    {
      title: 'Linda Squirrel',
      text: 'IT Induction Day reminder',
      date: moment().subtract(26, 'hour').toISOString()
    }
  ]
};

export default class MeView extends ReactComponent {

  render() {
    return (
      <div>
        <ListTile {...mail} />
        <CountTile {...mail} />
      </div>
    );
  }

}
