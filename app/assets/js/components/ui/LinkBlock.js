import React from 'react';
import ColumnLayout from './ColumnLayout';

export default class LinkBlock extends React.Component {

  render() {
    const { columns, children } = this.props;

    return (
      <div className="link-block">
        <ColumnLayout columns={ columns }>
          { children }
        </ColumnLayout>
      </div>
    );
  }

}

LinkBlock.propTypes = {
  columns: React.PropTypes.number.isRequired,
  children: React.PropTypes.arrayOf(React.PropTypes.node),
};

