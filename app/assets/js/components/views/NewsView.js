import $ from 'jquery';
import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import NewsCategoriesView from './NewsCategoriesView';
import NewsItem from '../ui/NewsItem';
import { connect } from 'react-redux';
import * as news from '../../state/news';
import * as newsCategories from '../../state/news-categories';
import InfiniteScrollable from '../ui/InfiniteScrollable';

export class NewsView extends React.Component {

  constructor(props) {
    super(props);
    this.onClickRefresh = this.fetch.bind(this);

    this.loadMore = this.loadMore.bind(this);

    this.state = {};
  }

  componentDidMount() {
    if (this.props.items.length === 0 && !this.props.failed) {
      this.fetch();
    }

    this.updateWidth();
  }

  componentDidUpdate() {
    this.updateWidth();
  }

  updateWidth() {
    const $node = $(ReactDOM.findDOMNode(this));
    const width = $node.width();

    if (width !== this.state.width) {
      this.setState({ width });
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
    const { failed, fetching, items, moreAvailable, user } = this.props;

    const width = this.state.width || this.props.width;

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

    if (width === undefined) {
      return <div />;
    }

    const itemComponents = items
      .map(item => <NewsItem key={item.id} width={width} {...item} />);

    const maybeMessage = !fetching ? <p>No news to show you yet.</p> : null;

    return (
      <div className="margin-top-1">
        { user && user.authenticated ?
          <NewsCategoriesView { ...this.props.newsCategories } dispatch={ this.props.dispatch } />
          : null
        }
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
  user: PropTypes.object.isRequired,
  width: PropTypes.number,
};

const select = (state) => ({
  ...state.news,
  newsCategories: state.newsCategories,
  user: state.user.data,
  // Never read, but kicks the component into updating when the device width changes
  deviceWidth: state.device.width,
});

export default connect(select)(NewsView);

