import AgendaTile, {
  AgendaTileItem,
  LargeBody
} from 'components/tiles/AgendaTile';
import Hyperlink from 'components/ui/Hyperlink';
import { shallow, render } from 'enzyme';

const now = new Date('2016-05-19T13:00:00+01:00');
const end = new Date('2016-05-19T14:00:00+01:00');
const today = new Date('2016-05-19T00:00:00Z').toISOString();
const tomorrow = new Date('2016-05-20T00:00:00Z').toISOString();

const ITEMS = {
  firstEvent: {
    id: '1',
    title: 'First Event',
    start: now.toISOString(),
    end: end.toISOString(),
    location: {
      name: 'Location'
    },
    organiser: {
      name: 'John Smith'
    },
  },
  secondEvent: {
    id: '2',
    title: 'Second Event',
    start: end.toISOString(),
    end: end.toISOString(),
    location: {
      name: 'Location'
    },
    organiser: {
      name: 'John Smith'
    },
  },
  twoWeeker: {
    id: 'xyz',
    start: '2016-05-16T00:00:00Z',
    end: '2016-05-28T00:00:00Z',
    isAllDay: true,
    title: 'Two-week event'
  }
};

describe('AgendaTile', () => {

  it('renders all-day events when small', () => {
    const content = {
      items: [
        {
          id: '1',
          title: 'AFD',
          start: today,
          end: tomorrow,
          isAllDay: true,
        },
      ],
    };

    const html = renderAtMoment(<AgendaTile size="small" content={ content }/>, now);

    findChild(html, [0, 0, 0, 1]).should.equal('All day today');
    findChild(html, [0, 0, 1, 1]).should.equal('AFD');
  });

  it('renders an event when small', () => {
    const content = {
      items: [ ITEMS.firstEvent ]
    };

    const html = renderAtMoment(<AgendaTile size="small" content={ content }/>, now);

    findChild(html, [0, 0, 0, 1]).should.equal('Today 13:00–14:00');
    findChild(html, [0, 0, 1, 1]).should.equal('First Event');
    findChild(html, [0, 0, 2, 1]).should.equal('Location');
    findChild(html, [0, 0, 3, 1]).should.equal('John Smith');
  });

  it('renders an event with the start time only when start = end time', () => {
    const content = {
      items: [ ITEMS.secondEvent ]
    };

    const html = renderAtMoment(<AgendaTile size="small" content={ content }/>, now);

    findChild(html, [0, 0, 0, 1]).should.equal('Today 14:00');
    findChild(html, [0, 0, 1, 1]).should.equal('Second Event');
    findChild(html, [0, 0, 2, 1]).should.equal('Location');
    findChild(html, [0, 0, 3, 1]).should.equal('John Smith');
  });

  it('include the weekday when rendering an event for tomorrow when small', () => {
    const content = {
      items: [
        {
          id: '1',
          title: 'Lunch tomorrow',
          start: '2016-05-20T12:00:00+01:00',
          end: '2016-05-20T14:00:00+01:00',
          isAllDay: false,
        },
      ],
    };

    const html = renderAtMoment(<AgendaTile size="small" content={ content }/>, now);

    findChild(html, [0, 0, 0, 1]).should.equal('Fri 12:00–14:00');
    findChild(html, [0, 0, 1, 1]).should.equal('Lunch tomorrow');
  });

  it('include the weekday when rendering an event for tomorrow when wide', () => {
    const content = {
      items: [
        {
          id: '1',
          title: 'Lunch tomorrow',
          start: '2016-05-20T12:00:00+01:00',
          end: '2016-05-20T14:00:00+01:00',
          isAllDay: false,
        },
      ],
    };

    const html = renderAtMoment(<AgendaTile size="wide" content={ content }/>, now);
    
    findChild(html, [0, 0, 0, 0, 0, 1]).should.equal('Fri 12:00–14:00');
    findChild(html, [0, 0, 0, 0, 1, 1]).should.equal('Lunch tomorrow');
  });

  it('renders a single time when start == end', () => {
    const content = {
      items: [
        {
          id: '1',
          title: 'Cinema',
          start: '2016-05-20T12:00:00+01:00',
          end: '2016-05-20T12:00:00+01:00',
          isAllDay: false,
        },
      ],
    };

    const html = renderAtMoment(<AgendaTile size="wide" content={ content }/>, now);

    findChild(html, [0, 0, 0, 0, 0, 1]).should.equal('Fri 12:00');
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
    atMoment(() => {
      const html = shallow(<AgendaTile size="wide" content={ content } />);
      html.find('.agenda__date').map(n => n.text()).should.deep.equal([
        ' Sat 12:00',
        ' Sat 12:00–14:00'
      ]);
    }, now);
  });

  it('renders a weekday only', () => {
    const itemSingleTime = {
      start: '2016-05-24T09:00:00+01:00',
      end: '2016-05-24T09:00:00+01:00',
    };
    const itemWithEnd = {
      start: '2016-05-24T09:00:00+01:00',
      end: '2016-05-24T10:30:00+01:00',
    };
    atMoment(() => {
      AgendaTile.renderSingleEventDate(itemSingleTime).should.equal('Tue 09:00');
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

    const html = renderAtMoment(<AgendaTile size="wide" content={content}/>, now);

    findChild(html, [0, 0, 0, 0, 0, 1]).should.equal('Today 13:00–14:00');
    findChild(html, [0, 0, 0, 0, 1, 1]).should.equal('First Event');

    findChild(html, [0, 1, 0, 0, 0, 1]).should.equal('Today 14:00');
    findChild(html, [0, 1, 0, 0, 1, 1]).should.equal('Second Event');
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

});

describe('LargeBody', () => {
  it('Groups by date', () => {
    const items = [ ITEMS.firstEvent, ITEMS.secondEvent ];
    const keys = items.map( n => n.id );
    const wrapper = shallow(<LargeBody>{ items }</LargeBody>);
    wrapper.props().children.map(n => n.key).should.deep.equal(keys);

    // could use enzyme.render() and check the actual HTML,
    // but instead just checking the function we'd use -
    // GroupedList should have its own tests.
    wrapper.name().should.equal('GroupedList');
    const grouper = wrapper.props().groupBy;
    const unixTime = now.getTime() / 1000;
    grouper.titleForGroup(unixTime).should.equal('Thu 19th May');
  });
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
    onClickLink: sinon.spy()
  };

  it('renders correctly without a href', () => {
    const html = shallow(<AgendaTileItem zoomed={ true } { ...props } />);
 
    html.hasClass('tile-list-item').should.equal(true);
    const titleElement = html.find('.tile-list-item__title');
    titleElement.props().title.should.equal(props.title);
    titleElement.find(Hyperlink).children().text().should.equal(props.title);

    html.find('.agenda-item__cell').first().text().should.equal('17:00 –18:00');
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
    locationInner.find('.fa-map-marker').exists().should.equal(true);

    const link = locationInner.find(Hyperlink);
    link.childAt(0).text().should.equal('Heronbank');
    link.props().href.should.equal('https://campus.warwick.ac.uk/?slid=29129');
  })
});
