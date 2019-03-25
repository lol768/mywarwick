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
    const html = shallowRender(<TextTile zoomed={ false } { ...props } />);
    const [ item ] = html.props.children;
    const hyperlink = shallowRender(item);
    hyperlink.type.should.equal('div');
    hyperlink.props.className.should.equal('tile__item');
    const [callout, calloutSub, text] = hyperlink.props.children;
    callout.type.should.equal('span');
    callout.props.className.should.equal('tile__callout');
    callout.props.children.should.equal(props.content.items[0].callout);
    should.equal(calloutSub, undefined);
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
    a.type.displayName.should.equal('Hyperlink');
    a.props.href.should.equal(props.content.items[0].href);
  });

  it('displays all items when zoomed', () => {
    const html = shallowRender(<TextTile zoomed={ true } { ...props } />);
    html.props.children[0].props.children.length.should.equal(2);
  });

});
