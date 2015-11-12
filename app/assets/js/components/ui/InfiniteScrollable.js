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
      $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]').on('scroll', this.boundScrollListener);

      this.onScroll();
    }
  }

  detachScrollListener() {
    $(window).off('scroll resize', this.boundScrollListener);

    $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]').off('scroll', this.boundScrollListener);
  }

  onScroll() {
    var $this = $(ReactDOM.findDOMNode(this));

    let offsetTop = $this.offset().top;
    let height = $this.height();

    let windowHeight = $(window).height();
    let scrollTop = $(window).scrollTop();

    let loadMoreThreshold = offsetTop + height - (windowHeight * 1.5);

    if (scrollTop >= loadMoreThreshold) {
      this.detachScrollListener();

      this.props.onLoadMore();
    }
  }

  render() {
    return <div>{this.props.children}</div>;
  }

}
