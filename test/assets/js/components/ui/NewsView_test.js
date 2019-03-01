import { NewsView } from 'components/views/NewsView';

describe('NewsView', () => {

  const defaults = {
    dispatch: () => {},
    failed: false,
    items: [],
    fetching: true,
    news: ['','',''],
    user: { authenticated: false },
    newsCategories: {
      data:{
        id: 'id1', name: 'veryStrangeThings9380182'
      },
    },
    width: 10,
    moreAvailable: true,
  };

  it('renders error message when failed to fetch news', () =>  {

    const props = {
      ...defaults,
      failed: true,
    };

    let result = shallowRender(<NewsView {...props} />);
    result.should.include("Unable to fetch news");

  });

});