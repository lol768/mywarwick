import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';

const SOME_MORE = 20;

export default class InfiniteScrollable extends ReactComponent {

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
    if (this.props.hasMore) {
      $(window).on('scroll resize', this.onScroll.bind(this));

      this.onScroll();
    }
  }

  detachScrollListener() {
    $(window).off('scroll resize', this.onScroll.bind(this));
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
