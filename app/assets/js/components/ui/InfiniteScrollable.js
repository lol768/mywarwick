import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';

export default class InfiniteScrollable extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {
      loading: false,
    };
    this.loadingShown = false;
    this.boundScrollListener = this.onScroll.bind(this);
  }

  componentDidMount() {
    this.attachScrollListener();
  }

  componentDidUpdate() {
    if (this.state.loading && !this.loadingShown) {
      this.loadingShown = true;
    }
    this.attachScrollListener();
  }

  componentWillUnmount() {
    this.detachScrollListener();
  }

  componentWillReceiveProps() {
    this.setState({
      loading: false,
    });
    this.loadingShown = false;
  }

  attachScrollListener() {
    this.detachScrollListener();
    this.detached = false;

    if (this.props.hasMore) {
      $(window).on('scroll resize', this.boundScrollListener);
      $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]')
        .on('scroll', this.boundScrollListener);

      this.onScroll();
    }
  }

  detachScrollListener() {
    this.detached = true;
    $(window).off('scroll resize', this.boundScrollListener);

    $(ReactDOM.findDOMNode(this)).parents('[data-scrollable]')
      .off('scroll', this.boundScrollListener);
  }

  onScroll() {
    if (this.detached) {
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
      if (!this.loadingShown) {
        this.setState({
          loading: true,
        });
      }
      this.props.onLoadMore();
    }
  }

  render() {
    return (<div>
        {this.props.children}
        { this.props.showLoading && this.state.loading ? <div className="loading-spinner centered">
            <i className="fa fa-spinner fa-pulse fa-2x" />
          </div> : ''
        }
      </div>);
  }

}
