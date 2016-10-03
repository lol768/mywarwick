import { NewsView } from 'components/views/NewsView';

describe('NewsView', () => {

  const defaults = {
    dispatch: () => {},
    failed: false,
    fetching: false,
    items: [],
    user: { authenticated: false },
    newsCategories: {},
    moreAvailable: true,
  };

  it('renders error message when failed to fetch news', () =>  {

    const props = {
      ...defaults,
      failed: true,
    };

    let result = shallowRender(<NewsView {...props} />);
    result.should.include("Unable to fetch news.");

  });

  it('render NewsCategoriesView for logged in users', () =>  {

    const props = {
      ...defaults,
      news: ['','',''],
      fetching: true,
      newsCategories: {
        data:{
          id: 'id1', name: 'veryStrangeThings9380182'
        },
      },
      user: { authenticated: true },
      width: 10,
    };

    const result = shallowRender(<NewsView {...props} />);

    result.should.include('veryStrangeThings9380182');
    result.should.include('NewsCategoriesView');

  });

  it('does not render NewsCategoriesView for guests', () =>  {

    const props = {
      ...defaults,
      news: ['','',''],
      fetching: true,
      newsCategories: {
        data:{
          id: 'id1', name: 'veryStrangeThings9380182'
        },
      },
      width: 10,
    };

    const result = shallowRender(<NewsView {...props} />);
    result.should.not.include('NewsCategoriesView');
    result.should.not.include('veryStrangeThings9380182');

  });
});