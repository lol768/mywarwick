import ListTile, { ListTileItem } from 'components/tiles/ListTile';

describe('ListTile', () => {

  const props = {
    content: {
      items: [{ id: 1 }, { id: 2 }, { id: 3 }, { id: 4 }],
      word: 'objects',
    },
    size: 'small',
  };

  it('should show only three items when not zoomed', () => {
    const html = shallowRender(<ListTile { ...props } />);
    html.type.should.equal('ul');
    html.props.children.length.should.equal(3);
  });

  it('should all items when zoomed', () => {
    const html = shallowRender(<ListTile zoomed={ true } { ...props } />);
    html.type.should.equal('ul');
    html.props.children.length.should.equal(4);
  });

});

describe('ListTileItem', () => {

  const props = {
    href: 'someUrl',
    title: 'a title',
    text: 'some text'
  };

  it('should render properly', () => {
    const html = shallowRender(<ListTileItem { ...props } />);
    html.type.should.equal('li');
    html.props.className.should.include('tile-list-item');
    const a = html.props.children;
    a.props.href.should.equal(props.href);
    const [title, , text] = a.props.children;
    title.type.should.equal('span');
    title.props.className.should.equal('list-group-item__title');
    title.props.children.should.equal(props.title);
    text.type.should.equal('span');
    text.props.className.should.equal('list-group-item__text');
    text.props.children.should.equal(props.text);
  });

  it('should render a date when one is present', () => {
    // must mock a ul or else render into document will refuse to render an orphaned li
    class MockedUl extends React.Component {
      render() {
        return <ul>{this.props.children}</ul>;
      }
    }

    const html = ReactTestUtils.renderIntoDocument(
      <MockedUl><ListTileItem date={ '2015-10-27 08:27:00.000' } {... props} /></MockedUl>
    );
    const date = ReactTestUtils.findRenderedDOMComponentWithClass(html, 'list-group-item__date');
    expect(date.textContent).to.equal('Tue 27 Oct 2015, 8:27');
  });
});
