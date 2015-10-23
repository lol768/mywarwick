import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import _ from 'lodash';

export default class LinkBlock extends ReactComponent {

    render() {
        const TOTAL_COLUMNS = 12;

        let cols = parseInt(this.props.columns);
        let chunked = _.chunk(this.props.children, Math.ceil(this.props.children.length / cols));

        let columns = chunked.map((items, i) => {
            return (
                <ul key={"col-" + i} className={"list-unstyled link-block__column col-xs-" + (TOTAL_COLUMNS / cols)}>
                    {items}
                </ul>
            );
        });

        return (
            <div className="link-block row">
                {columns}
            </div>
        );
    }

}
