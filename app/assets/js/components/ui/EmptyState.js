import React from 'react';
import * as PropTypes from 'prop-types';

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
