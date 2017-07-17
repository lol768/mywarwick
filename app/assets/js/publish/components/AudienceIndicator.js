import React from 'react';
import * as PropTypes from 'prop-types';

export default function AudienceIndicator(props) {
  if (props.fetching) {
    return (
      <div className="alert alert-info">
        <i className="fa fa-spin fa-refresh"> </i>
      </div>
    );
  }

  const { baseAudience, categorySubset } = props;

  if (props.public) {
    return (
      <div className="alert alert-info">
        <div>Public audience</div>
      </div>
    );
  }

  const baseNum = baseAudience !== undefined ? baseAudience.toLocaleString() : '0';
  const catNum = categorySubset !== undefined && `${categorySubset.toLocaleString()}/${baseNum}`;

  return (
    <div className="alert alert-info">
      <div className="pull-right">
        <i className="fa fa-info-circle" data-toggle="tooltip" data-placement="left"
          title="Estimated audience size will be shown here, when audience and categories
        have been selected"
        />
      </div>
      <div>{`People in this audience: ${baseNum}`}</div>
      {categorySubset !== undefined && <div>{`Interested in these categories: ${catNum}`}</div>}
    </div>
  );
}

AudienceIndicator.propTypes = {
  fetching: PropTypes.bool,
  error: PropTypes.bool,
  empty: PropTypes.bool,
  public: PropTypes.bool,
  baseAudience: PropTypes.number,
  categorySubset: PropTypes.number,
};
