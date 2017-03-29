import $ from 'jquery';
import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import ReactPullToRefresh from 'react-pull-to-refresh';
import NewsCategoriesView from './NewsCategoriesView';
import NewsItem from '../ui/NewsItem';
import { connect } from 'react-redux';
import * as news from '../../state/news';
import * as newsCategories from '../../state/news-categories';
import InfiniteScrollable from '../ui/InfiniteScrollable';

export class NewsView extends React.Component {

  constructor(props) {
    super(props);
    this.fetch = this.fetch.bind(this);
    this.onRefresh = this.onRefresh.bind(this);

    this.loadMore = this.loadMore.bind(this);

    this.state = {
      refreshing: false,
    };
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

  onRefresh(resolve, reject) {
    this.setState({
      refreshing: true,
    });

    function unsetRefreshing() {
      this.setState({
        refreshing: false,
      });
    }

    this.fetch()
      .then(resolve, reject)
      .then(unsetRefreshing, unsetRefreshing);
  }

  updateWidth() {
    const $node = $(ReactDOM.findDOMNode(this));
    const width = $node.width();

    if (width !== this.state.width) {
      this.setState({ width });
    }
  }

  loadMore() {
    return Promise.resolve(this.props.dispatch(news.fetch()));
  }

  fetch() {
    return Promise.all([
      this.props.dispatch(news.refresh()),
      this.props.dispatch(newsCategories.fetch()),
    ]);
  }

  render() {
    const { failed, fetching, items, moreAvailable, user } = this.props;
    const { refreshing } = this.state;

    const width = this.state.width || this.props.width;

    if (failed) {
      return (
        <div className="alert alert-warning">
          <p>
            Unable to fetch news.
          </p>
          <p>
            <button type="button" onClick={ this.fetch } className="btn btn-default">
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

    const maybeMessage = !fetching ?
      <div className="container-fluid">
        <p>No news to show you yet.</p>
      </div> : null;

    const content = (
      <div className="margin-top-1">
        { user && user.authenticated ?
          <NewsCategoriesView { ...this.props.newsCategories } dispatch={ this.props.dispatch } />
          : null
        }
        { items.length ?
          <InfiniteScrollable hasMore={moreAvailable} onLoadMore={this.loadMore}>
            {itemComponents}
          </InfiniteScrollable>
          : maybeMessage
        }
        { fetching && !refreshing ?
          <div className="centered">
            <i className="fa fa-lg fa-refresh fa-spin"> </i>
          </div> : null }
      </div>
    );

    if (this.props.inTile) {
      return content;
    }

    return (
      <ReactPullToRefresh
        loading={<span>
            <i className="fa fa-lg fa-refresh"> </i>
            <i className="fa fa-lg fa-refresh fa-spin"> </i>
          </span>}
        onRefresh={this.onRefresh}
      >
        {content}
      </ReactPullToRefresh>
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
  inTile: PropTypes.bool,
};

const select = (state) => ({
  ...state.news,
  newsCategories: state.newsCategories,
  user: state.user.data,
  // Never read, but kicks the component into updating when the device width changes
  pixelWidth: state.device.pixelWidth,
});

export default connect(select)(NewsView);

