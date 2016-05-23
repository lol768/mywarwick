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
  const allDayDate = new Date('Thur 18 May 2016 13:00');

  it('displays a limited number of items when not zoomed', () => {
    const html = shallowRender(<AgendaTile { ...props } maxItemsToDisplay={ 1 }/>);
    html.props.children.length.should.equal(1);
  });

  it('renders all-day events when small', () => {
    const allDayProps = buildSmallProps([{ id: '1', title: 'AFD' }]);
    const html = renderAtMoment(<AgendaTile { ...allDayProps } />, allDayDate);
    findChild(html, [1, 0, 0]).should.equal('All day: AFD');
  });

  it('renders multiple all-day events text', () => {
    const allDayProps = buildSmallProps([{ id: '1', title: 'AFD1' }, { id: '2', title: 'AFD2' }]);
    const html = renderAtMoment(<AgendaTile { ...allDayProps } />, allDayDate);
    findChild(html, [1, 0, 0]).should.equal('You have 2 all day events');
  });

  it('only render all day event in absence of timed events', () => {
    const allDayProps = buildSmallProps([{ id: '1' }, {
      id: '1',
      title: 'timed',
      end: allDayDate.toISOString()
    }]);
    const html = renderAtMoment(<AgendaTile { ...allDayProps } />, allDayDate);
    findChild(html, [1, 0, 0]).should.equal('Next: timed at 13:00');
  });

  it('Displays all items when zoomed', () => {
    const html = shallowRender(<AgendaTile
      maxItemsToDisplay={ 1 }
      zoomed={ true }
      { ...props }
    />);
    html.props.children.length.should.equal(3);
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
    const html = shallowRender(<AgendaTileItem zoomed={ true } { ...props } end={ undefined }/>);
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
