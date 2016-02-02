import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';

export default class InfiniteScrollable extends ReactComponent {

  constructor(props) {
    super(props);

    this.boundScrollListener = this.onScroll.bind(this);
  }

  componentDidMount() {
    this.attachScrollListener();
  }

  componentDidUpdate() {
    this.attachScrollListener();
  }

  componentWillUnmount() {
    this.detachScrollListener();
  }

  attachScrollListener() {
    this.detachScrollListener();

    if (this.props.hasMore) {
      $(window).on('scroll resize', this.boundScrollListener);
      $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]')
        .on('scroll', this.boundScrollListener);

      this.onScroll();
    }
  }

  detachScrollListener() {
    $(window).off('scroll resize', this.boundScrollListener);

    $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]')
      .off('scroll', this.boundScrollListener);
  }

  onScroll() {
    const $this = $(ReactDOM.findDOMNode(this));

    const offsetTop = $this.offset().top;
    const height = $this.height();

    const windowHeight = $(window).height();
    const scrollTop = $(window).scrollTop();

    const loadMoreThreshold = offsetTop + height - (windowHeight * 1.5);

    if (scrollTop >= loadMoreThreshold) {
      this.detachScrollListener();

      this.props.onLoadMore();
    }
  }

  render() {
    return <div>{this.props.children}</div>;
  }

}
