import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import wrapKeyboardSelect from '../../../keyboard-nav';
import * as colourSchemes from '../../../state/colour-schemes';
import HideableView from '../HideableView';

class ColourSchemesView extends HideableView {
  static propTypes = {
    fetching: PropTypes.bool.isRequired,
    dispatch: PropTypes.func.isRequired,
    failed: PropTypes.bool.isRequired,
    fetched: PropTypes.bool.isRequired,
    chosen: PropTypes.number.isRequired,
    schemes: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.number.isRequired,
      url: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    })).isRequired,
    isOnline: PropTypes.bool.isRequired,
  };


  constructor(props) {
    super(props);
    this.onSelect = this.onSelect.bind(this);
    this.persist = this.persist.bind(this);
  }

  onSelect(chosen) {
    if (!this.props.isOnline) return;
    this.persist(chosen);
  }

  persist(chosen) {
    this.props.dispatch(colourSchemes.changeColourScheme(chosen));
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(colourSchemes.fetch());
  }

  makeItem(scheme) {
    return (
      <div
        className={`list-group-item list-group-item--colour-scheme list-group-item__choice--colour-scheme list-group-item__choice--colour-scheme-${scheme.id}-preview`}
        role="button"
        key={ scheme.id }
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

function select(store) {
  return {
    fetching: store.colourSchemes.fetching,
    failed: store.colourSchemes.failed,
    fetched: store.colourSchemes.fetched,
    chosen: store.colourSchemes.chosen,
    schemes: store.colourSchemes.schemes,
    isOnline: store.device.isOnline,
  };
}

export default connect(select)(ColourSchemesView);
