import $ from 'jquery';
import React from 'react';
import * as PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import NewsItem from '../ui/NewsItem';
import * as news from '../../state/news';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import HideableView from './HideableView';

export class NewsView extends HideableView {
  constructor(props) {
    super(props);
    this.onClickRefresh = this.fetch.bind(this);

    this.loadMore = this.loadMore.bind(this);

    this.state = {};
  }

  componentDidShow() {
    if (!this.props.failed) {
      if (this.props.items.length === 0) {
        this.props.dispatch(news.fetch());
      }
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
    return Promise.resolve(this.props.dispatch(news.fetch()));
  }

  fetch() {
    this.props.dispatch(news.fetch());
  }

  render() {
    const {
      failed,
      fetching,
      items,
      moreAvailable,
    } = this.props;

    const width = this.state.width || this.props.width;

    if (failed) {
      return (
        <div className="inset-alert">
          <div className="alert alert-warning">
            <p>
              <i className="fal fa-fw fa-times" /> Unable to fetch news.
            </p>
            <p>
              <button type="button" onClick={ this.onClickRefresh } className="btn btn-default">
                <i className="fal fa-fw fa-sync" />
                Retry
              </button>
            </p>
          </div>
        </div>
      );
    }

    if (width === undefined) {
      return <div />;
    }

    const itemComponents = items
      .map(item => <NewsItem key={item.id} width={width} {...item} />);

    const maybeMessage = !fetching
      ? (<div className="inset-alert">
        <div className="alert alert-info">
          <p>
            <i className="fal fa-lightbulb-slash" /> There is no news to show you yet.
          </p>
        </div>
      </div>) : null;

    return (
      <div>
        { items.length
          ? <ScrollRestore url={`/${Routes.NEWS}`}>
            <InfiniteScrollable
              hasMore={moreAvailable}
              onLoadMore={this.loadMore}
              endOfListPhrase="There is no older news"
            >
              {itemComponents}
            </InfiniteScrollable>
          </ScrollRestore> : maybeMessage
        }
        { fetching
          ? <div className="centered">
            <i className="fal fa-lg fa-sync fa-spin" />
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
  moreAvailable: PropTypes.bool.isRequired,
  user: PropTypes.object.isRequired,
  width: PropTypes.number,
};

const select = state => ({
  ...state.news,
  user: state.user.data,
  // Never read, but kicks the component into updating when the device width changes
  pixelWidth: state.device.pixelWidth,
});

export default connect(select)(NewsView);
