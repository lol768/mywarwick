import { NewsItemImage } from 'components/ui/NewsItem';

describe('NewsItemImage', () => {

  it('renders an image tag', () => {
    const result = shallowRender(<NewsItemImage width={123.5} alt="Some alt text" id="abc"/>);

    result.should.deep.equal(
      <div className="news-item__image">
        <img src="/api/news/images/abc?width=124" alt="Some alt text"/>
      </div>
    );
  });

});
