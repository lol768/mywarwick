import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import NewsItem from '../ui/NewsItem';
import CheckableListItem from '../ui/CheckableListItem';

import { connect } from 'react-redux';
import _ from 'lodash';

import { fetchNews } from '../../serverpipe';

class NewsView extends ReactComponent {

  componentDidMount() {
    if (this.props.items.length == 0 && !this.props.failed)
      this.props.dispatch(fetchNews());
  }

  render() {
    if (this.props.fetching) {
      return <i className="fa fa-refresh fa-spin"></i>;
    }

    if (this.props.failed) {
      return (
        <div className="alert alert-warning">
          <p>
            Unable to fetch news.
          </p>
          <p>
            <button onClick={() => this.props.dispatch(fetchNews())} className="btn btn-default">
              <i className="fa fa-refresh fa-fw"></i>
              Retry
            </button>
          </p>
        </div>
      );
    }

    let html = (content) => ({__html: content.replace(/<br[ /]+?>/g, '')});

    let items = _.take(this.props.items, 5).map((item) =>
      <NewsItem key={item.id} title={item.title} source={item.source} url={item.url.href}>
        <div dangerouslySetInnerHTML={html(item.content)}></div>
      </NewsItem>
    );

    return (
      <div>
        {items}
      </div>
    );
  }

}

let select = (state) => state.get('news').toJS();

export default connect(select)(NewsView);
