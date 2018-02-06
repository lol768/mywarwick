import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { AudiencePicker } from './AudiencePicker';
import AudienceIndicator from './AudienceIndicator';

export class NesCategoryPucker extends React.PureComponent {

  static propTypes = {
    newsCategories: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string,
      name: PropTypes.string,
    })),
    updateAudienceIndicator: PropTypes.func,
  };

  constructor(props) {
    super(props);
    this.updateAudienceIndicator = props.updateAudienceIndicator;
  }
  render() {
    return(
      <div>
        hello there!
      </div>
    );
  }
}