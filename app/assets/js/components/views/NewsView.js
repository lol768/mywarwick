import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import NewsItem from '../ui/NewsItem';

import { connect } from 'react-redux';
import _ from 'lodash';

import * as news from '../../state/news';
import InfiniteScrollable from '../ui/InfiniteScrollable';

const SOME_MORE = 10;

class NewsView extends ReactComponent {

  constructor(props) {
    super(props);
    this.onClickRefresh = this.onClickRefresh.bind(this);

    this.state = {
      numberToShow: SOME_MORE,
    };
    this.loadMore = this.loadMore.bind(this);
  }

  loadMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE,
    });
  }

  componentDidMount() {
    if (this.props.items.length === 0 && !this.props.failed) {
      this.props.dispatch(news.fetch());
    }
  }

  onClickRefresh() {
    this.props.dispatch(news.fetch());
  }

  render() {
    if (this.props.fetching) {
      return <i className="centered fa fa-lg fa-refresh fa-spin"> </i>;
    }

    if (this.props.failed) {
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

    if (this.props.items.length) {
      const items = _.take(this.props.items, this.state.numberToShow).map((item) =>
        <NewsItem
          key={item.id}
          {...item}
        />
      );

      const hasMore = this.state.numberToShow < this.props.items.length;

      return (
        <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore}>
          {items}
        </InfiniteScrollable>
      );
    }

    return (<p>No news to show you yet.</p>);
  }

}

const select = (state) => state.news;

export default connect(select)(NewsView);
