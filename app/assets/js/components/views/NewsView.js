import React, { PropTypes } from 'react';
import NewsCategoriesView from './NewsCategoriesView';
import NewsItem from '../ui/NewsItem';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as news from '../../state/news';
import * as newsCategories from '../../state/news-categories';
import InfiniteScrollable from '../ui/InfiniteScrollable';

const SOME_MORE = 10;

class NewsView extends React.Component {

  constructor(props) {
    super(props);
    this.onClickRefresh = this.fetch.bind(this);

    this.state = {
      numberToShow: SOME_MORE,
    };
    this.loadMore = this.loadMore.bind(this);
  }

  componentDidMount() {
    if (this.props.items.length === 0 && !this.props.failed) {
      this.fetch();
    }
  }

  loadMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE,
    });
  }

  fetch() {
    this.props.dispatch(news.fetch());
    this.props.dispatch(newsCategories.fetch());
  }

  render() {
    const { failed, fetching, items } = this.props;
    const { numberToShow } = this.state;
    const hasMore = numberToShow < items.length;

    if (failed) {
      return (
        <div className="alert alert-warning">
          <p>
            Unable to fetch news.
          </p>
          <p>
            <button onClick={ this.onClickRefresh } className="btn btn-default">
              <i className="fa fa-refresh fa-fw"> </i>
              Retry
            </button>
          </p>
        </div>
      );
    }

    const itemComponents = _.take(items, numberToShow).map((item) =>
      <NewsItem
        key={item.id}
        {...item}
      />
    );

    return (
      <div>
        <NewsCategoriesView { ...this.props.newsCategories } dispatch={ this.props.dispatch } />
        { fetching ? <i className="centered fa fa-lg fa-refresh fa-spin"> </i> : null }
        { items.length ?
          <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore}>
            {itemComponents}
          </InfiniteScrollable> :
          <p>No news to show you yet.</p>
        }
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
};

const select = (state) => ({
  ...state.news,
  newsCategories: state.newsCategories,
});

export default connect(select)(NewsView);
