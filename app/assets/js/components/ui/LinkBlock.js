import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import _ from 'lodash';

export default class LinkBlock extends ReactComponent {

  render() {
    const TOTAL_COLUMNS = 12;

    const cols = parseInt(this.props.columns, 10);
    const chunked = _.chunk(this.props.children, Math.ceil(this.props.children.length / cols));

    const columns = chunked.map((items, i) => { // eslint-disable-line arrow-body-style
      return (
        <ul
          key={`col-${i}`}
          className={`list-unstyled link-block__column col-xs-${TOTAL_COLUMNS / cols}`}
        >
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
