import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class ListHeader extends ReactComponent {

    render() {
        return (
            <div className="list-header">
                {this.props.title}
            </div>
        );
    }

}
