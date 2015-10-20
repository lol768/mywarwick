const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const NewsItem = require('../ui/NewsItem');
const CheckableListItem = require('../ui/CheckableListItem');

export default class NewsView extends ReactComponent {

    render() {
        return (
            <div>
                <div className="list-group">
                    <CheckableListItem color="#3f5c98" text="Computer Science" />
                    <CheckableListItem color="#7ecbb6" text="Insite" />
                    <CheckableListItem color="#b03865" text="Warwick Arts Centre" checked />
                    <CheckableListItem color="#e73f97" text="Warwick Retail" />
                    <CheckableListItem color="#410546" text="Warwick SU" checked />
                </div>
                <NewsItem title="DIMAP Logic Day 2015"
                          source="DCS">
                    <p>
                        On June 1st 2015, our Division of Theory and Foundations, jointly with DIMAP, organized DIMAP Logic Day 2015. The
                        goal of the event was to bring together the UK community of researchers and graduate students interested in the
                        study of logics, automata and games.
                    </p>
                </NewsItem>
            </div>
        );
    }

}
