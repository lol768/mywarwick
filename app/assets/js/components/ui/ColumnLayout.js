import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';

export default class ColumnLayout extends React.PureComponent {
  render() {
    const TOTAL_COLUMNS = 12;
    const { columns, children } = this.props;

    const cols = children.map((item, i) => (<div key={ i } className={`col-xs-${TOTAL_COLUMNS / columns}`}>
        { item }
      </div>));

    const rows = _.chunk(cols, columns).map((columnsInRow, i) => (<div className="row" key={ i }>
        { columnsInRow }
      </div>));

    return <div>{ rows }</div>;
  }
}

ColumnLayout.propTypes = {
  columns: PropTypes.number.isRequired,
  children: PropTypes.arrayOf(PropTypes.node),
};
