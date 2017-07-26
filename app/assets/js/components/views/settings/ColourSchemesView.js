import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import wrapKeyboardSelect from '../../../keyboard-nav';

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


  constructor(props) {
    super(props);
    this.state = props;
  }

  onClick(e, choiceId) {
    alert(e.type);

    e.target.blur();
    this.onSelect(choiceId);
  }

  onSelect(choiceId) {
    if (!this.state.isOnline) return;
    this.setState({'chosen': choiceId});
  }

  makeItem(scheme) {
    return (
      <div
        className={`list-group-item list-group-item-colour-scheme list-group-item-colour-scheme-choice list-group-item-colour-scheme-choice-${scheme.id}`}
        role="button"
        onClick={ e => wrapKeyboardSelect(() => this.onSelect(scheme.id), e) }
        onKeyUp={ e => wrapKeyboardSelect(() => this.onSelect(scheme.id), e) }
        tabIndex={ 0 }
      >
        <div className="media media-colour-scheme-choice">
          <div className="media media-colour-scheme-block">
            <div className="media-left">
              <div className="md-radio-colour-scheme-choice">
                <input
                  type="radio"
                  checked={ scheme.id === this.state.chosen }
                  readOnly
                  disabled={ !this.state.isOnline }
                />
                <label />
              </div>
            </div>
            <div className="media-body media-body-colour-scheme-choice">
              { scheme.name }
            </div>
          </div>

          <div className="media-right" />
        </div>
      </div>
    );
  }

  render() {
    return (
      <div>
        <div className="list-group fixed">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Colour scheme</h3>
            </div>
          </div>
        </div>

        <div className="list-group">
          {_.map(this.props.schemes, scheme => this.makeItem(scheme))}
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
