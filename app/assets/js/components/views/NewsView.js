import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import NewsItem from '../ui/NewsItem';

import { connect } from 'react-redux';
import _ from 'lodash';

import { fetchNews } from '../../serverpipe';
import InfiniteScrollable from '../ui/InfiniteScrollable';

const SOME_MORE = 10;

class NewsView extends ReactComponent {

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);

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
      this.props.dispatch(fetchNews());
    }
  }

  onClick() {
    this.props.dispatch(fetchNews());
  }

  render() {
    if (this.props.fetching) {
      return <i className="fa fa-refresh fa-spin"> </i>;
    }

    if (this.props.failed) {
      return (
        <div className="alert alert-warning">
          <p>
            Unable to fetch news.
          </p>
          <p>
            <button onClick={ this.onClick } className="btn btn-default">
              <i className="fa fa-refresh fa-fw"> </i>
              Retry
            </button>
          </p>
        </div>
      );
    }

    const html = (content) => ({ __html: content.replace(/<br[ /]+?>/g, '') });

    const items = _.take(this.props.items, this.state.numberToShow).map((item) =>
      <NewsItem
        key={item.id}
        {...item}
      >
        <div dangerouslySetInnerHTML={html(item.content)}></div>
      </NewsItem>
    );

    const hasMore = this.state.numberToShow < this.props.items.length;

    return (
      <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore}>
        {items}
      </InfiniteScrollable>
    );
  }

}

const select = (state) => state.get('news').toJS();

export default connect(select)(NewsView);
