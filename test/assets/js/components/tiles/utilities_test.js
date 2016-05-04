import { Hyperlink } from 'components/tiles/utilities';

describe('Hyperlink', () => {
  const href = "https://google.com";
  const child = "wrap me";

  it('wraps child in hyperlink', () => {
    const html = shallowRender(<Hyperlink key="1" href={href} >{ child }</Hyperlink>);
    html.type.should.equal('a');
    expect(html).jsx.to.include(child);
  });

  it('returns child as React element if href null', () => {
    const html = shallowRender(<Hyperlink key="1" href={null} >{ child }</Hyperlink>);
    html.type.should.equal('span');
    html.props.children.should.equal('wrap me');
  });
});
