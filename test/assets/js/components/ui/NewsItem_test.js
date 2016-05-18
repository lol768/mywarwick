import NewsItem, { render } from 'components/ui/NewsItem';

describe('render', () => {

  it('renders a single line as a paragraph', () => {
    const result = render('Wizard News');
    result.should.deep.equal([<p>Wizard News</p>]);
  });

  it('renders a mixture of newlines and whitespace as paragraphs', () => {
    const result = render('Para 1.\nPara 2. \n \nPara 3.\n\n\n   Para 4.');
    result.should.deep.equal([
      <p>Para 1.</p>,
      <p>Para 2.</p>,
      <p>Para 3.</p>,
      <p>Para 4.</p>,
    ]);
  });

});
