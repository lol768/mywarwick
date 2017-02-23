import React, { Component, PropTypes } from 'react';
import _ from 'lodash';
import $ from 'jquery';

export default class TileOptionView extends Component {

  constructor(props) {
    super(props);

    const defaultPref = {};
    _.forOwn(props.tile.option, (value, key) => {
      defaultPref[key] = value.default;
    });
    this.state = {
      currentPreferences: {
        ...defaultPref,
        ...props.tile.preferences || {},
      },
    };

    this.saveConfig = this.saveConfig.bind(this);
    this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
    this.handleRadioChange = this.handleRadioChange.bind(this);
  }

  makeFormBody(formId) {
    const options = this.props.tile.option;
    const optionsKeys = Object.keys(options);

    return (
      <form className="form" id={formId}>
        {optionsKeys.map(key => <div key={key} className="form-group">
          <label>{key}:</label>{this.makeOptionElement(options[key], key)}
        </div>)}
      </form>
    );
  }

  makeOptionElement(option, key) {
    switch (option.type.toLowerCase()) {
      case 'array':
        return option.options.map(e => this.makeCheckbox(e, key));
      case 'string':
        return option.options.map(e => this.makeRadioBox(e, key));
      default:
        return (
          <div />
        );
    }
  }

  makeCheckbox(possibleChoice, cbName) {
    return (
      <div className="checkbox" key={`${cbName}:${possibleChoice.value}`}>
        <label>
          <input
            type="checkbox"
            id={possibleChoice.value}
            value={possibleChoice.value}
            name={cbName}
            checked={
              (this.state.currentPreferences[cbName] &&
              this.state.currentPreferences[cbName].includes(possibleChoice.value)) ? true : null
            }
            onChange={ this.handleCheckboxChange }
          />
          {possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        </label>
      </div>
    );
  }

  makeRadioBox(possibleChoice, radioName) {
    return (
      <div key={`${radioName}:${possibleChoice.value}`} className="radio">
        <label>
          <input
            type="radio"
            name={radioName}
            id={possibleChoice.value}
            value={possibleChoice.value}
            checked={
              (this.state.currentPreferences[radioName] &&
              this.state.currentPreferences[radioName] === possibleChoice.value) ? true : null
            }
            onChange={ this.handleRadioChange }
          />
          {possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        </label>
      </div>
    );
  }

  handleRadioChange(event) {
    const target = event.target;
    const value = target.value;
    const checked = target.checked;
    const currentPref = _.clone(this.state.currentPreferences, true);
    const name = target.name;

    if (checked) {
      this.setState({
        currentPreferences: {
          ...currentPref,
          [name]: value,
        },
      });
    }
  }

  handleCheckboxChange(event) {
    const target = event.target;
    const value = target.value;
    const checked = target.checked;
    const name = target.name;
    const currentPref = _.clone(this.state.currentPreferences, true);

    if (checked) {
      const items = (currentPref[name] || []).concat([value]);
      this.setState({
        currentPreferences: {
          ...currentPref,
          [name]: items,
        },
      });
    } else {
      const currentItems = currentPref[name];
      let items = [];
      if (currentItems) {
        items = currentPref[name].filter(e => !(e === value));
      }

      this.setState({
        currentPreferences: {
          ...currentPref,
          [name]: items,
        },
      });
    }
  }

  saveConfig() {
    const formId = `#config-${this.props.tile.id}-form`;
    const preferences = $(formId).serializeArray();
    const tile = this.props.tile;
    this.props.onConfigSave(tile, preferences);
  }

  render() {
    return (
      <div
        className="tile--config__modal fade in"
        id={`config-${this.props.tile.id}`}
        tabIndex="-1"
        role="dialog"
        aria-labelledby="myModalLabel"
      >
        <div className="tile--config__modal_dialog" role="document">
          <div className="tile--config__modal_content">
            <div className="modal-header">
              <button
                type="button"
                className="close"
                data-dismiss="modal"
                aria-label="Close"
              >
                <span
                  aria-hidden="true"
                  onClick={ this.props.onConfigViewDismiss }
                >
                  &times;
                </span>
              </button>
              <h4 className="modal-title" id={`title-${this.props.tile.id}`}>Change settings
                for {this.props.tile.title}</h4>
            </div>
            <div className="tile--config__modal_body">
              { this.makeFormBody(`config-${this.props.tile.id}-form`) }
            </div>
            <div className="tile--config__modal_footer">
              <button
                type="button"
                className="btn btn-default"
                data-dismiss="modal"
                onClick={ this.props.onConfigViewDismiss }
              >
                Close
              </button>
              <button type="button" className="btn btn-primary" onClick={ this.saveConfig }>
                Save changes
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

TileOptionView.propTypes = {
  tile: PropTypes.object.isRequired,
  onConfigViewDismiss: PropTypes.func.isRequired,
  onConfigSave: PropTypes.func.isRequired,
};
