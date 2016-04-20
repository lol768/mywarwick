import AgendaTile from 'components/tiles/AgendaTile';
import { AgendaTileItem } from 'components/tiles/AgendaTile';

describe('AgendaTile', () => {

  const props = {
    "content": {
      "items": [{ id: '1' }, { id: '2' }, { id: '3' }]
    },
    size: 'large',
  };

  it('Displays a limited number of items when not zoomed', () => {
    const html = shallowRender(<AgendaTile { ...props } maxItemsToDisplay={ 1 }/>);
    html.props.children.length.should.equal(1);
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
    titleInner.props.children.should.equal(props.title);
    dateInner.should.equal('17:00');
  });

  it('renders with a href. clicking it calls the onClickLink prop', () => {
    const tileItem = <AgendaTileItem zoomed={ true } href={ 'href' } { ...props } />;
    const html = shallowRender(tileItem);
    const a = html.props.children.props.children[1].props.children[0].props.children;
    a.props.href.should.equal('href');
    a.type.should.equal('a');
    const node = ReactTestUtils.renderIntoDocument(tileItem);
    ReactTestUtils.Simulate.click(node.refs.a);
    props.onClickLink.should.have.been.called;
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
    locationInner.props.children[1].props.children.should.equal('Heronbank');
    locationInner.props.children[1].props.href.should.equal('https://campus.warwick.ac.uk/?slid=29129');
  })

});
