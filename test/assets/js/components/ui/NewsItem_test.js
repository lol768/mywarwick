import NewsItem, { render } from 'components/ui/NewsItem';

describe('render', () => {

  it('renders a single line as a paragraph', () => {
    const result = render('Wizard News');
    result.should.deep.equal([<p key={ 0 }>Wizard News</p>]);
  });

  it('renders a mixture of newlines and whitespace as paragraphs', () => {
    const result = render('Para 1.\nPara 2. \n \nPara 3.\n\n\n   Para 4.');
    result.should.deep.equal([
      <p key={ 0 }>Para 1.</p>,
      <p key={ 1 }>Para 2.</p>,
      <p key={ 2 }>Para 3.</p>,
      <p key={ 3 }>Para 4.</p>,
    ]);
  });

});
