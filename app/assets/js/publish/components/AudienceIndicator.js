import React from 'react';

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

  const baseNum = baseAudience !== undefined ?
    baseAudience.toLocaleString() : '0';
  const catNum = categorySubset !== undefined ?
    `${categorySubset.toLocaleString()}/${baseNum}` : `0/${baseNum}`;

  return (
    <div className="alert alert-info">
      <div className="pull-right">
        <i className="fa fa-info-circle" data-toggle="tooltip" data-placement="left"
          title="Estimated audience size will be shown here, when audience and categories
        have been selected"
        />
      </div>
      <div>{`People in this audience: ${baseNum}`}</div>
      <div>{`Interested in these categories: ${catNum}`}</div>
    </div>
  );
}

AudienceIndicator.propTypes = {
  fetching: React.PropTypes.bool,
  error: React.PropTypes.bool,
  empty: React.PropTypes.bool,
  public: React.PropTypes.bool,
  baseAudience: React.PropTypes.number,
  categorySubset: React.PropTypes.number,
};
