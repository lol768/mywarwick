import { shallow } from 'enzyme';
import React from 'react';
import AgendaTile from 'components/tiles/agenda/AgendaTile';
import LargeBody, { AgendaTileItem } from 'components/tiles/agenda/LargeBody';
import Hyperlink from 'components/ui/Hyperlink';
import SingleEvent from 'components/tiles/agenda/SingleEvent';

import { ITEMS, now } from './data';

describe('AgendaTile', () => {

  it('renders a weekday only', () => {
    const itemSingleTime = {
      start: '2016-05-24T09:00:00+01:00',
    };
    const itemDuplicateTime = {
      start: '2016-05-24T09:00:00+01:00',
      end: '2016-05-24T09:00:00+01:00',
    };
    const itemWithEnd = {
      start: '2016-05-24T09:00:00+01:00',
      end: '2016-05-24T10:30:00+01:00',
    };
    atMoment(() => {
      AgendaTile.renderSingleEventDate(itemSingleTime).should.equal('Tue 09:00');
      AgendaTile.renderSingleEventDate(itemDuplicateTime).should.equal('Tue 09:00');
      AgendaTile.renderSingleEventDate(itemWithEnd).should.equal('Tue 09:00–10:30');
    }, now);
  });

  it('renders two events side-by-side when wide', () => {
    const content = {
      items: [
        ITEMS.firstEvent,
        ITEMS.secondEvent,
      ]
    };

    const result = shallowAtMoment(<AgendaTile size="wide" content={content}/>, now);

    result.find(SingleEvent).should.have.length(2);

  });

  it('displays all items when zoomed', () => {
    const content = {
      items: [
        { id: '1' }, { id: '2' }, { id: '3' },
      ],
    };
    const html = shallowRender(<AgendaTile
      zoomed={ true }
      size="small"
      content={ content }
    />);

    html.props.children.length.should.equal(3);
  });

  it('displays multiple-day all-day events on each future day within the range', () => {
    const content = {
      items: [ ITEMS.twoWeeker ]
    };
    const html = renderAtMoment(
      <AgendaTile
        zoomed={ true }
        content={ content }
        size="large"
      />
      , now);
    html.props.children.length.should.equal(9);
  });

  it('displays single-day all-day events', () => {
    const content = {
      items: [ ITEMS.singleDayAllDay ]
    };
    const html = renderAtMoment(
      <AgendaTile
        zoomed={ true }
        content={ content }
        size="large"
      />
      , now);
    html.props.children.length.should.equal(1);
  });
});

describe('LargeBody', () => {
  it('Groups by date', () =>
    atMoment(() => {
      const items = [ITEMS.firstEvent, ITEMS.secondEvent];
      const keys = items.map(n => n.id);
      const wrapper = shallow(<LargeBody>{items}</LargeBody>);
      wrapper.props().children.map(n => n.key).should.deep.equal(keys);

      // could use enzyme.render() and check the actual HTML,
      // but instead just checking the function we'd use -
      // GroupedList should have its own tests.
      wrapper.name().should.equal('GroupedList');
      const grouper = wrapper.props().groupBy;
      const unixTime = now.getTime() / 1000;
      grouper.titleForGroup(unixTime).should.equal('Thu 19th May');
    }, new Date(2016, 5, 1))
  );

  it('includes year in group titles for non-current years', () =>
    atMoment(() => {
      const items = [ITEMS.firstEvent, ITEMS.secondEvent];
      const wrapper = shallow(<LargeBody>{items}</LargeBody>);

      // could use enzyme.render() and check the actual HTML,
      // but instead just checking the function we'd use -
      // GroupedList should have its own tests.
      const grouper = wrapper.props().groupBy;
      const unixTime = now.getTime() / 1000;
      grouper.titleForGroup(unixTime).should.equal('Thu 19th May 2016');
    }, new Date(2015, 5, 1))
  );
});

describe('AgendaTileItem', () => {
  const props = {
    start: '2014-08-04T17:00:00',
    end: '2014-08-04T18:00:00',
    title: 'Heron hunting',
    location: {
      name: 'Heronbank',
      href: 'https://campus.warwick.ac.uk/?slid=29129',
    },
  };

  it('handles staff prop as array', () => {
    const staffMember = {
      email: 'E.L.Blagrove@warwick.ac.uk',
      firstName: 'Dr Nic',
      lastName: 'Duke',
      universityId: '123456',
      userType: 'Staff'
    };
    const propsWithStaff = {...props, staff: [staffMember]};
    const html = shallow(<AgendaTileItem zoomed={ true } { ...propsWithStaff } />);

    const staffInner = html.find('.tile-list-item__organiser');
    expect(staffInner.html()).to.include(`${staffMember.firstName} ${staffMember.lastName}`)
  });

  it('handles staff prop as empty array', () => {
    const propsWithEmptyStaff = {...props, staff: []};
    const html = shallow(<AgendaTileItem zoomed={ true } { ...propsWithEmptyStaff } />);

    const staffInner = html.find('.tile-list-item__organiser');
    expect(staffInner.length).to.equal(0)
  });

  it('renders correctly without a href', () => {
    const html = shallow(<AgendaTileItem zoomed={ true } { ...props } />);

    html.hasClass('tile-list-item').should.equal(true);
    const titleElement = html.find('.tile-list-item__title');
    titleElement.props().title.should.equal(props.title);
    html.find(Hyperlink).first().children().text().should.equal(props.title);
    html.find('.agenda-item__cell').first().find('.agenda-item__cell__times__start-time').text().should.equal('17:00');
    html.find('.agenda-item__cell').first().find('.agenda-item__cell__times__end-time').text().should.equal('18:00');
  });

  it('renders with a href', () => {
    const tileItem = <AgendaTileItem zoomed={ true } href={ 'href' } { ...props } />;
    const html = shallow(tileItem);
    const hyperlink = html.find(Hyperlink).first();
    hyperlink.props().href.should.equal('href');
    hyperlink.name().should.equal('Hyperlink');
  });

  it('renders time for All day events', () => {
    const html = shallow(<AgendaTileItem zoomed={ true } { ...props } isAllDay={ true }/>);
    const date = html.find('.agenda-item__cell').first().children();
    date.text().should.equal('All day');
  });

  it('renders location text with hyperlink', () => {
    const html = shallow(<AgendaTileItem zoomed={ true } { ...props } />);

    const locationInner = html.find('.tile-list-item__location');
    locationInner.hasClass('text--light').should.equal(true);
    locationInner.contains('Heronbank').should.equal(true);
    locationInner.html().should.include('<i class="fa fa-map-marker"></i>');

    const link = locationInner.find(Hyperlink);
    link.childAt(0).text().should.equal('Heronbank');
    link.props().href.should.equal('https://campus.warwick.ac.uk/?slid=29129');
  });

  it('renders multiple locations', () => {
    const props2 = {
      ...props,
      location: [
        {
          name: 'Location',
        },
        {
          name: 'Location',
        },
        {
          name: 'Location',
        },
      ],
    };

    const html = shallow(<AgendaTileItem zoomed={ true } { ...props2 } />);

    const locationInner = html.find('.tile-list-item__location');
    locationInner.hasClass('text--light').should.equal(true);
    locationInner.contains('Location, Location, Location').should.equal(true);
  });
});