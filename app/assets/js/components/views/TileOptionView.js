import React, { Component, PropTypes } from 'react';

export default class TileOptionView extends Component {

  makeFormBody(formId) {
    const options = this.props.tile.option;
    const optionsKeys = Object.keys(options);

    return (
      <form className="form" id={formId}>
        {optionsKeys.map(key => <div className="form-group">
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

  makeCheckbox(possibleChoice, checkboxName) {
    return (
      <div className="checkbox">
        <label>
          <input
            type="checkbox"
            id={possibleChoice.value}
            value={possibleChoice.value}
            name={checkboxName}
          />
          {possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        </label>
      </div>
    );
  }

  makeRadioBox(possibleChoice, radioName) {
    return (
      <div className="radio">
        <label>
          <input
            type="radio"
            name={radioName}
            id={possibleChoice.value}
            value={possibleChoice.value}
          />
          {possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        </label>
      </div>
    );
  }

  saveConfig() {
    const preferences = $(`#config-${this.props.tile.id}`).serializeArray();
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
          <div className="modal-content">
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
            <div className="modal-footer">
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
