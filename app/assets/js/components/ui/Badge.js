import React from 'react';

const formatBadgeCount = (n) => (n > 99 ? '99+' : n);

export default function Badge(props) {
  if (props.count > 0) {
    return (
      <span className="badge" {...props}>{ formatBadgeCount(props.count) }</span>
    );
  }

  return <span />;
}

Badge.propTypes = {
  count: React.PropTypes.number.isRequired,
};
