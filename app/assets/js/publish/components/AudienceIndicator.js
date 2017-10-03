import React from 'react';
import * as PropTypes from 'prop-types';
import Hyperlink from '../../components/ui/Hyperlink';

export default function AudienceIndicator(props) {
  if (props.fetching) {
    return (
      <div className="alert alert-info">
        <i className="fa fa-spin fa-refresh" />
      </div>
    );
  }

  const { baseAudience } = props;

  if (props.public) {
    return (
      <div className="alert alert-info">
        <div>Public audience</div>
      </div>
    );
  }

  const baseNum = baseAudience !== undefined ? baseAudience.toLocaleString() : '0';

  return (
    <div className="alert alert-info">
      <div>
        <p>When sending alerts, please remember that alerts should be specific or personal to the recipient, and something they need to be aware of or take action on immediately, and concise - a sentence or two at most. <Hyperlink href="https://warwick.ac.uk/mw-support/faqs/usingalerts">More info...</Hyperlink></p>
      </div>

      <div className="pull-right">
        <i
          className="fa fa-info-circle"
          data-toggle="tooltip"
          data-placement="left"
          title="Estimated audience size will be shown here, when audience and categories
        have been selected"
        />
      </div>
      <div>{`This alert will be published to ${baseNum} people.`}</div>
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
