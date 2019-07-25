import Hyperlink from 'components/ui/Hyperlink';
import { shallow } from 'enzyme';

describe('Hyperlink', () => {

  it('renders plain text if no href', () => {
    const result = shallowRender(<Hyperlink>Wizard News</Hyperlink>);
    result.should.deep.equal(<span>Wizard News</span>);
  });

  it('renders a link if href is present', () => {
    const newsHref = 'https://hogwarts.example.com/news';
    const result = shallow(<Hyperlink href={newsHref}>Wizard News</Hyperlink>);
    result.html().should.equal(`<a href="${newsHref}" target="_blank" rel="noopener noreferrer">Wizard News</a>`);
  })

});
