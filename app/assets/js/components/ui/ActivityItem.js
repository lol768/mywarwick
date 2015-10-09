const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const moment = require('moment');

export default class ActivityItem extends ReactComponent {

    render() {
        return (
            <div className="activity-item">
                <span className="activity-item__text">{this.props.text}</span>
                <span className="activity-item__source">{this.props.source}</span>
                <span className="activity-item__date">{moment(this.props.date).calendar()}</span>
            </div>
        );
    }

}