import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import moment from 'moment';

import AppIcon from './AppIcon';

export default class ActivityItem extends ReactComponent {

    render() {
        return (
            <div className="activity-item">
                <div className="media">
                    <div className="media-left">
                        <AppIcon app={this.props.source} size="lg" />
                    </div>
                    <div className="media-body">
                        <span className="activity-item__text">{this.props.text}</span>
                        <span className="activity-item__source">{this.props.source}</span>
                        <span className="activity-item__date">{moment(this.props.date).calendar()}</span>
                    </div>
                </div>
            </div>
        );
    }

}