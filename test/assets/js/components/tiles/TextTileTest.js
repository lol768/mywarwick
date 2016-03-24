import TextTile from 'components/tiles/TextTile';

describe('TextTile', () => {

  const props = {
    "content":{
      "href":"http://warwick.ac.uk/",
      "items":[
        { "id":'1', "callout":"Big text", "text":"Little text" },
        { "id":'2', "callout":"Heron", "text":"an evil creature" }
      ]
    },
    size: 'small',
  };

  it('initially renders the first item', () => {
    const html = shallowRender(<TextTile zoomed={ true } { ...props } />);
    const [ item ] = html.props.children;
    item.type.should.equal('div');
    item.props.className.should.equal('tile__item');
    const [callout, text] = item.props.children;
    callout.type.should.equal('span');
    callout.props.className.should.equal('tile__callout');
    callout.props.children.should.equal(props.content.items[0].callout);
    text.type.should.equal('span');
    text.props.className.should.equal('tile__text');
    text.props.children.should.equal(props.content.items[0].text);
  });

  it('wraps the items in an anchor if it has a link', () => {
    const props = {
      "content":{
        "href":"http://warwick.ac.uk/",
        "items":[
          { "id":"1", "callout":"Big text", "text":"Little text", "href":"http://warwick.ac.uk/" },
        ]
      },
      size: 'small',
    };
    const html = shallowRender(<TextTile { ...props } />);
    const [ a ] = html.props.children;
    a.type.should.equal('a');
    a.props.href.should.equal(props.content.items[0].href);
  });

  it('displays all items when zoomed', () => {
    const html = shallowRender(<TextTile zoomed={ true } { ...props } />);
    html.props.children.length.should.equal(2);
  });

});
