import AgendaTile, { AgendaTileItem } from 'components/tiles/AgendaTile';

describe('AgendaTile', () => {

  const content = {
    items: [
      { id: '1' }, { id: '2' }, { id: '3' },
    ],
  };

  const now = new Date('2016-05-19T13:00:00+01:00');
  const end = new Date('2016-05-19T14:00:00+01:00');
  const today = new Date('2016-05-19T00:00:00Z').toISOString();
  const tomorrow = new Date('2016-05-20T00:00:00Z').toISOString();

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

    findChild(html, [0, 0, 1]).should.equal('All day today');
    findChild(html, [0, 1, 1]).should.equal('AFD');
  });

  it('renders an event when small', () => {
    const content = {
      items: [
        {
          id: '1',
          title: 'Sample Event',
          start: now.toISOString(),
          end: end.toISOString(),
          location: {
            name: 'Location'
          },
          organiser: {
            name: 'John Smith'
          },
        },
      ]
    };

    const html = renderAtMoment(<AgendaTile size="small" content={ content }/>, now);

    findChild(html, [0, 0, 1]).should.equal('13:00–14:00');
    findChild(html, [0, 1, 1]).should.equal('Sample Event');
    findChild(html, [0, 2, 1]).should.equal('Location');
    findChild(html, [0, 3, 1]).should.equal('John Smith');
  });

  it('renders two events side-by-side when wide', () => {
    const content = {
      items: [
        {
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
        {
          id: '1',
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
      ],
    };

    const html = renderAtMoment(<AgendaTile size="wide" content={content}/>, now);

    findChild(html, [0, 0, 0, 0, 1]).should.equal('13:00–14:00');
    findChild(html, [0, 0, 0, 1, 1]).should.equal('First Event');

    findChild(html, [0, 1, 0, 0, 1]).should.equal('14:00–14:00');
    findChild(html, [0, 1, 0, 1, 1]).should.equal('Second Event');
  });

  it('displays all items when zoomed', () => {
    const html = shallowRender(<AgendaTile
      zoomed={ true }
      size="small"
      content={ content }
    />);

    html.props.children.length.should.equal(3);
  });

  it('displays multiple-day all-day events on each future day within the range', () => {
    const content = {
      items: [
        {
          id: 'xyz',
          start: '2016-05-16T00:00:00Z',
          end: '2016-05-28T00:00:00Z',
          isAllDay: true,
          title: 'Two-week event'
        }
      ]
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
    const html = shallowRender(<AgendaTileItem zoomed={ true } { ...props } />);
    html.type.should.equal('div');
    html.props.className.should.equal('tile-list-item');
    const a = html.props.children;
    const [date, , title] = a.props.children;
    const titleInner = title.props.children[1],
      dateInner = date.props.children;
    titleInner.props.className.should.equal(
      'tile-list-item__title'
    );
    titleInner.props.title.should.equal(props.title);
    findChild(titleInner, [0, 0]).should.equal(props.title);
    dateInner.should.equal('17:00');
  });

  it('renders with a href', () => {
    const tileItem = <AgendaTileItem zoomed={ true } href={ 'href' } { ...props } />;
    const html = shallowRender(tileItem);
    const hyperlink = html.props.children.props.children[2].props.children[1].props.children;
    hyperlink.props.href.should.equal('href');
    hyperlink.type.displayName.should.equal('Hyperlink');
  });

  it('renders time for All day events', () => {
    const html = shallowRender(<AgendaTileItem zoomed={ true } { ...props } isAllDay={ true }/>);
    const a = html.props.children;
    const [date] = a.props.children;
    date.props.children.should.equal('All day');
  });

  it('renders location text with hyperlink', () => {
    const html = shallowRender(<AgendaTileItem zoomed={ true } { ...props } />);
    const locationInner = html.props.children
      .props.children[2].props.children[3];
    locationInner.props.className.should.equal(
      'tile-list-item__location text--light'
    );
    findChild(locationInner, [1, 0]).should.equal('Heronbank');
    locationInner.props.children[1].props.href.should.equal('https://campus.warwick.ac.uk/?slid=29129');
  })
});
