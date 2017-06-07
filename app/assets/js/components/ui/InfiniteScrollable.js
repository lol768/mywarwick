import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import log from 'loglevel';
import * as notifications from '../../state/notifications';
import { makeCancelable } from '../../promise';
import HideableView from '../views/HideableView';

export default class InfiniteScrollable extends HideableView {

  static propTypes = {
    hasMore: PropTypes.bool,
    onLoadMore: PropTypes.func.isRequired,
    children: PropTypes.node,
    showLoading: PropTypes.bool,
    endOfListPhrase: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.state = {
      loading: false,
    };
    this.unmounted = false;
    this.boundScrollListener = this.onScroll.bind(this);
    this.cancellableShowMorePromise = makeCancelable(Promise.resolve());
  }

  componentWillUnmount() {
    this.unmounted = true;
  }

  componentDidShow() {
    this.attachScrollListener();
  }

  componentWillHide() {
    this.detachScrollListener();
    this.cancellableShowMorePromise.cancel();
  }

  onScroll() {
    if (this.detached || this.unmounted) {
      return;
    }

    const $this = $(ReactDOM.findDOMNode(this));

    const offsetTop = $this.offset().top;
    const height = $this.height();

    const windowHeight = $(window).height();
    const scrollTop = $(window).scrollTop();

    const loadMoreThreshold = offsetTop + height - (windowHeight * 1.5);

    if (scrollTop >= loadMoreThreshold) {
      this.detachScrollListener();
      this.setState({ loading: true });
      this.cancellableShowMorePromise = makeCancelable(this.props.onLoadMore());
      this.cancellableShowMorePromise.promise.then(() => {
        if (!this.unmounted) this.setState({ loading: false });
      }).catch((e) => {
        if (this.unmounted) return;
        if (e.isCanceled) {
          return;
        } else if (!(e instanceof notifications.UnnecessaryFetchError)) {
          this.setState({ loading: false });
        } else {
          log.debug(`Unnecessary fetch: ${e.message}`);
          return;
        }
        throw e;
      });
    }
  }

  detachScrollListener() {
    this.detached = true;
    $(window).off('scroll resize', this.boundScrollListener);

    if (!this.unmounted) {
      $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]')
        .off('scroll', this.boundScrollListener);
    }
  }

  attachScrollListener() {
    this.detachScrollListener();
    this.detached = false;

    if (!this.state.loading && this.props.hasMore) {
      $(window).on('scroll resize', this.boundScrollListener);
      $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]')
        .on('scroll', this.boundScrollListener);

      this.onScroll();
    }
  }

  noMoreItems(phrase) {
    return (
      <div className="centered empty-state">
        <p className="lead">{ phrase }</p>
      </div>
    );
  }

  render() {
    return (<div>
        {this.props.children}
        { this.props.showLoading && this.state.loading ? <div className="loading-spinner centered">
            <i className="fa fa-spinner fa-pulse fa-2x" />
          </div> : ''
        }
      { this.props.hasMore || this.noMoreItems(this.props.endOfListPhrase) }
      </div>);
  }

}
