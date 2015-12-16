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
        <div className="alert alert-warning" onClick={() => this.props.dispatch(fetchNews())}>
          Unable to fetch news.  Click to retry.
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
        <div className="list-group">
          <CheckableListItem color="#3f5c98" text="Computer Science"/>
          <CheckableListItem color="#7ecbb6" text="Insite" checked/>
          <CheckableListItem color="#b03865" text="Warwick Arts Centre"/>
          <CheckableListItem color="#e73f97" text="Warwick Retail"/>
          <CheckableListItem color="#410546" text="Warwick SU"/>
        </div>
        {items}
      </div>
    );
  }

}

let select = (state) => state.get('news').toJS();

export default connect(select)(NewsView);
