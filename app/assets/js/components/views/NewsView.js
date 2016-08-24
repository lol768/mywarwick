import React, { PropTypes } from 'react';
import NewsCategoriesView from './NewsCategoriesView';
import NewsItem from '../ui/NewsItem';
import { connect } from 'react-redux';
import * as news from '../../state/news';
import * as newsCategories from '../../state/news-categories';
import InfiniteScrollable from '../ui/InfiniteScrollable';

class NewsView extends React.Component {

  constructor(props) {
    super(props);
    this.onClickRefresh = this.fetch.bind(this);

    this.loadMore = this.loadMore.bind(this);
  }

  componentDidMount() {
    if (this.props.items.length === 0 && !this.props.failed) {
      this.fetch();
    }
  }

  loadMore() {
    this.props.dispatch(news.fetch());
  }

  fetch() {
    this.props.dispatch(news.fetch());
    this.props.dispatch(newsCategories.fetch());
  }

  render() {
    const { failed, fetching, items, moreAvailable } = this.props;

    if (failed) {
      return (
        <div className="alert alert-warning">
          <p>
            Unable to fetch news.
          </p>
          <p>
            <button type="button" onClick={ this.onClickRefresh } className="btn btn-default">
              <i className="fa fa-refresh fa-fw"> </i>
              Retry
            </button>
          </p>
        </div>
      );
    }

    const itemComponents = items.map(item => <NewsItem key={item.id} {...item} />);

    const maybeMessage = !fetching ? <p>No news to show you yet.</p> : null;

    return (
      <div className="margin-top-1">
        <NewsCategoriesView { ...this.props.newsCategories } dispatch={ this.props.dispatch } />
        { items.length ?
          <InfiniteScrollable hasMore={moreAvailable} onLoadMore={this.loadMore}>
            {itemComponents}
          </InfiniteScrollable> : maybeMessage
        }
        { fetching ?
          <div className="centered">
            <i className="fa fa-lg fa-refresh fa-spin"></i>
          </div> : null }
      </div>
    );
  }

}

NewsView.propTypes = {
  dispatch: PropTypes.func.isRequired,
  failed: PropTypes.bool.isRequired,
  fetching: PropTypes.bool.isRequired,
  items: PropTypes.array.isRequired,
  newsCategories: PropTypes.object.isRequired,
  moreAvailable: PropTypes.bool.isRequired,
};

const select = (state) => ({
  ...state.news,
  newsCategories: state.newsCategories,
});

export default connect(select)(NewsView);
