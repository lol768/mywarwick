
import { Hyperlink } from 'components/tiles/utilities';

describe('Hyperlink', () => {

  it('renders plain text if no href', () => {
    const result = shallowRender(<Hyperlink child="Wizard News" />);
    result.should.equal('Wizard News');
  })

  it('renders a link if href is present', () => {
    const newsHref = 'https://hogwarts.example.com/news';
    const result = shallowRender(<Hyperlink child="Wizard News" href={newsHref} />)
    result.should.deep.equal(<a href={newsHref} target="_blank">Wizard News</a>);
  })

});