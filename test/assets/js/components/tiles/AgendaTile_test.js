import moment from 'moment';
import AgendaTile from 'components/tiles/AgendaTile';
import { AgendaTileItem } from 'components/tiles/AgendaTile';

describe('AgendaTile', () => {

  const props = {
    "content": {
      "items": [{ id: '1' }, { id: '2' }, { id: '3' }]
    },
    size: 'large',
  };

  const buildSmallProps = items => ({ content: { items }, size: 'small' });
  const now = new Date('2016-05-19T13:00:00+01:00');
  const end = new Date('2016-05-19T14:00:00+01:00');
  const today = new Date('2016-05-19T00:00:00Z').toISOString();
  const tomorrow = new Date('2016-05-20T00:00:00Z').toISOString();

  it('displays a limited number of items when not zoomed', () => {
    const html = shallowRender(<AgendaTile { ...props } maxItemsToDisplay={ 1 }/>);
    html.props.children.length.should.equal(1);
  });

  it('renders all-day events when small', () => {
    const allDayProps = buildSmallProps([{ id: '1', title: 'AFD', start: today, end: tomorrow, isAllDay: true }]);
    const html = renderAtMoment(<AgendaTile { ...allDayProps } />, now);
    console.log(html.props.children);
    findChild(html, [1, 0, 0]).should.equal('All day: AFD');
  });

  it('renders multiple all-day events text', () => {
      const allDayProps = buildSmallProps([{ id: '1', title: 'AFD1', start: today, end: tomorrow, isAllDay: true }, { id: '2', title: 'AFD2', start: today, end: tomorrow, isAllDay: true }]);
    const html = renderAtMoment(<AgendaTile { ...allDayProps } />, now);
    findChild(html, [1, 0, 0]).should.equal('You have 2 all day events');
  });

  it('only renders all day event in absence of timed events', () => {
    const allDayProps = buildSmallProps([{ id: '1' }, {
      id: '1',
      title: 'timed',
      start: now.toISOString(),
      end: end.toISOString()
    }]);
    const html = renderAtMoment(<AgendaTile { ...allDayProps } />, now);
    findChild(html, [1, 0, 0]).should.equal('Next: timed at 13:00');
  });

  it('displays all items when zoomed', () => {
    const html = shallowRender(<AgendaTile
      maxItemsToDisplay={ 1 }
      zoomed={ true }
      { ...props }
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
    const [ date, title ] = a.props.children;
    const titleInner = title.props.children[0],
      dateInner = date.props.children;
    titleInner.props.className.should.equal(
      'tile-list-item__title text--align-bottom'
    );
    titleInner.props.title.should.equal(props.title);
    findChild(titleInner, [0, 0]).should.equal(props.title);
    dateInner.should.equal('17:00');
  });

  it('renders with a href', () => {
    const tileItem = <AgendaTileItem zoomed={ true } href={ 'href' } { ...props } />;
    const html = shallowRender(tileItem);
    const hyperlink = html.props.children.props.children[1].props.children[0].props.children;
    hyperlink.props.href.should.equal('href');
    hyperlink.type.displayName.should.equal('Hyperlink');
  });

  it('renders time for All day events', () => {
    const html = shallowRender(<AgendaTileItem zoomed={ true } { ...props } isAllDay={ true }/>);
    const a = html.props.children;
    const [ date , ] = a.props.children;
    date.props.children.should.equal('All day');
  });

  it('renders location text with hyperlink', () => {
    const html = shallowRender(<AgendaTileItem zoomed={ true } { ...props } />);
    const locationInner = html.props.children
      .props.children[1].props.children[1];
    locationInner.props.className.should.equal(
      'tile-list-item__location text--align-bottom text--light'
    );
    findChild(locationInner, [1, 0]).should.equal('Heronbank');
    locationInner.props.children[1].props.href.should.equal('https://campus.warwick.ac.uk/?slid=29129');
  })

});
