import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';

import ActivityItem from '../ui/ActivityItem';

import { connect } from 'react-redux';

import { getStreamPartition } from '../../stream';

const SOME_MORE = 20;

class NotificationsView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      numberToShow: SOME_MORE
    };
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
    if (this.hasMore()) {
      $(window).on('scroll resize', this.onScroll.bind(this));

      this.onScroll();
    }
  }

  detachScrollListener() {
    $(window).off('scroll resize', this.onScroll.bind(this));
  }

  hasMore() {
    return this.props.notifications.count() > this.state.numberToShow;
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

      this.setState({
        numberToShow: this.state.numberToShow + SOME_MORE
      });
    }
  }

  render() {
    let notifications = this.props.notifications
      .take(this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} {...n} />);

    return <div>{notifications}</div>;
  }

}

function select(state) {
  return {
    notifications: getStreamPartition(state.get('notifications'), 0)
  };
}

export default connect(select)(NotificationsView);
