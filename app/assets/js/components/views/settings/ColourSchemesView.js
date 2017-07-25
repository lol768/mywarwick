import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import classNames from 'classnames';
import _ from 'lodash-es';
import RadioListGroupItem from '../../ui/RadioListGroupItem';
import { connect } from 'react-redux';

class ColourSchemesView extends React.PureComponent {
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
        className={`list-group-item list-group-item-colour-scheme list-group-item-colour-scheme-choice list-group-item-colour-scheme-choice-${scheme.id}`}
        role="button"
        onClick={ this.onClick }
      >
        <div className="media media-colour-scheme-choice">
          <div className="media media-colour-scheme-block">
            <div className="media-left">
              <div className="md-radio-colour-scheme-choice">
                <input
                  type="radio"
                  checked={ scheme.id === this.props.chosen }
                  readOnly
                  disabled={ !this.props.isOnline }
                />
                <label />
              </div>
            </div>
            <div className="media-body media-body-colour-scheme-choice">
              { scheme.name }
            </div>
          </div>
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
        <div className="list-group fixed">
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
