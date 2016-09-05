import React from 'react';

export default function AudienceIndicator(props) {
  if (props.error || props.empty) {
    return <div />;
  }

  if (props.fetching) {
    return (
      <div className="alert alert-info">
        <i className="fa fa-spin fa-refresh"> </i>
      </div>
    );
  }

  const { baseAudience, categorySubset } = props;

  let parts = [];

  if (props.public) {
    parts.push(
      <div>
        Public audience
      </div>
    );
  }

  if (baseAudience !== undefined) {
    parts.push(
      <div>
        People in this audience: {baseAudience.toLocaleString()}
      </div>
    );

    if (categorySubset !== undefined) {
      parts.push(
        <div>
          Interested in these categories: {categorySubset.toLocaleString()}
          /{baseAudience.toLocaleString()}
        </div>
      );
    }
  }

  return (
    <div className="alert alert-info">
      {parts}
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
