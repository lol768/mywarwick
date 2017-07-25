import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import _ from 'lodash-es';
import RadioListGroupItem from '../../ui/RadioListGroupItem';
import { connect } from 'react-redux';

class ColourSchemesView {
  static propTypes = {
    fetching: PropTypes.bool.isRequired,
    failed: PropTypes.bool.isRequired,
    fetched: PropTypes.bool.isRequired,
    chosen: PropTypes.number.isRequired,
    schemes: PropTypes.shape({
      id: PropTypes.number.isRequired,
      url: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    }).isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  makeRadioItem(choice, groupName) {
    const chosen = this.props.chosen;
    return (
      <RadioListGroupItem
        key={ `${name}:${choice.id}` }
        icon={ `cog` }
        description={ choice.name }
        onClick={ this.onClick }
        checked={ choice.id === chosen }
        name={ groupName }
        value={ choice.id }
        disabled={ !this.props.isOnline }
      />
    );
  }

  makeItem(scheme) {
    return (
      <div
        className="list-group-item"
        role="button"
        onClick={ this.onClick }
      >
        <div className="media">


          <div className="media-left">
            <div className="md-radio">
              <input
                type="radio"
                checked={ scheme.id === this.props.chosen }
                readOnly
                disabled={ !this.props.isOnline }
              />
              <label />
            </div>
          </div>

          <div
            className={ classNames('media-body', { 'media-body-disabled': !this.props.isOnline })}>
            { scheme.name }
          </div>
          {scheme.name}
          <div className="media-right">
          </div>
        </div>
      </div>
    );
  }

  onClick() {

  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-0">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Colour schemes</h3>
            </div>
          </div>
        </div>

        <div className="list-group">
          {_.map(this.props.schemes, (scheme) => {
            return this.makeItem(scheme);
          })}
        </div>
      </div>
    );
  }
}

function select(state) {
  return {
    fetching: state.colourSchemes.fetching,
    failed: state.colourSchemes.failed,
    fetched: state.colourSchemes.fetched,
    chosen: state.colourSchemes.chosen,
    schemes: state.colourSchemes.schemes,
    isOnline: state.device.isOnline,
  };
}

export default connect(select)(ColourSchemesView);
