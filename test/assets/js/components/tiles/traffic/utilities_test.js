import { Hyperlink } from 'components/tiles/utilities';

describe('Hyperlink', () => {
  const props = {
    href: "https://google.com",
    child: "wrap me",
  };
  
  it('wraps child in hyperlink', () => {
    const html = shallowRender(<Hyperlink key="1" {...props} />);
    html.type.should.equal('a');
    expect(html).jsx.to.include(<span>wrap me</span>);
  });

  it('returns child as React element if href null', () => {
    const html = shallowRender(<Hyperlink key="1" {...props} href={null} />);
    html.type.should.equal('span');
    html.props.children.should.equal('wrap me');
  });
});
