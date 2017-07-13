import React from 'react';
import * as PropTypes from 'prop-types';

export default class EmptyState extends React.PureComponent {
  static propTypes = {
    lead: PropTypes.string,
    children: PropTypes.node,
  };

  render() {
    const { lead, children } = this.props;
    return (
      <div className="empty-state">
        { lead ? <p className="lead">{ lead }</p> : null }
        { children }
      </div>
    );
  }
}
