const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const NewsItem = require('../ui/NewsItem');

export default class NewsView extends ReactComponent {

    render() {
        return (
            <div>
                <NewsItem title="DIMAP Logic Day 2015"
                          source="DCS"
                          imgSrc="https://www2.warwick.ac.uk/fac/sci/dcs/news/audience.jpg">
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
