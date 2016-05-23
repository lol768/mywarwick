import React, { PropTypes } from 'react';

export const EmptyState = ({ lead, children }) => (
  <div className="empty-state">
    { lead ? <p className="lead">{ lead }</p> : null }
    { children }
  </div>
);

EmptyState.propTypes = {
  lead: PropTypes.string,
  children: PropTypes.node,
};

export default EmptyState;
