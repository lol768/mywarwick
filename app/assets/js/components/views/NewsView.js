import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import NewsItem from '../ui/NewsItem';
import CheckableListItem from '../ui/CheckableListItem';

import { connect } from 'react-redux';
import { fetchNews } from '../../actions';

class NewsView extends ReactComponent {

    componentDidMount() {
        if (this.props.items.length == 0)
            this.props.dispatch(fetchNews());
    }

    render() {
        let html = (content) => ({__html: content.replace(/<br[ /]+?>/g, '')});

        let items = this.props.items.map((item) =>
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
