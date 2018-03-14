import React from 'react';
import SingleEvent from 'components/tiles/agenda/SingleEvent';

import { ITEMS, now } from './data';

describe('SingleEvent', () => {
  function extractDate(query) {
    return query.find('.agenda__date').childAt(1).text();
  }

  it('renders a single time when start == end', () => {
    const event =
      {
        id: '1',
        title: 'Cinema',
        start: '2016-05-20T12:00:00+01:00',
        end: '2016-05-20T12:00:00+01:00',
        isAllDay: false,
      };

    const html = shallowAtMoment(<SingleEvent event={event} />, now);

    extractDate(html).should.equal('Fri 12:00');
  });

  it('include the weekday when rendering an event for tomorrow', () => {
    const event =
      {
        id: '1',
        title: 'Lunch tomorrow',
        start: '2016-05-20T12:00:00+01:00',
        end: '2016-05-20T14:00:00+01:00',
        isAllDay: false,
      };

    const html = shallowAtMoment(<SingleEvent event={event} />, now);

    extractDate(html).should.equal('Fri 12:00–14:00');
    html.find('li').at(1).text().should.include ('Lunch tomorrow');
  });

  it('renders a date for tomorrow', () => {
    const content = {
      items: [
        {
          id: '1',
          title: 'Cinema',
          start: '2016-05-21T12:00:00+01:00',
          end: '2016-05-21T12:00:00+01:00',
        },
        {
          id: '2',
          title: 'Fun',
          start: '2016-05-21T12:00:00+01:00',
          end: '2016-05-21T14:00:00+01:00',
        },
      ],
    };

    const event0 = shallowAtMoment(<SingleEvent event={content.items[0]} />, now);
    extractDate(event0).should.equal('Sat 12:00');

    const event1 = shallowAtMoment(<SingleEvent event={content.items[1]} />, now);
    extractDate(event1).should.equal('Sat 12:00–14:00');

  });

  it('renders an event with multiple locations when small', () => {
    const event =
      {
        ...ITEMS.firstEvent,
        location: [
          { name: 'Location', },
          { name: 'Location?', },
          { name: 'Location!', },
        ]
      };

    const html = shallowAtMoment(<SingleEvent event={event} />, now);
    const items = html.find('li');
    extractDate(html).should.equal('Today 13:00–14:00');
    items.at(1).text().should.equal('First Event');
    items.at(2).childAt(1).text().should.equal('Location, Location?, Location!');
    items.at(3).childAt(1).text().should.equal('John Smith');
  });

  /*it('includes the parent in the title', () => {
    const event = ITEMS.parentOnly;

    const html = shallowAtMoment(<SingleEvent event={event} />, now);
    html.find('li').at(1).text().should.equal('IN101 Introduction to IT');
  });*/

  it('renders all-day events when small', () => {
    const event = ITEMS.singleDayAllDay;

    const html = shallowAtMoment(<SingleEvent event={event} />, now);

    extractDate(html).should.equal('All day Thu 16 Jun');
  });

});