import React, { Component } from 'react';
import classNames from 'classnames';

export default class TileOptionView extends Component {

  constructor(props) {
    super(props);
  }

  makeFormBody() {
    const option = this.props.tile.option;
    const optionsKeys = Object.keys(option);

    return (
      <div>
        <form className="form">
          <div>
            {optionsKeys.map(key => <div className="form-group">
              <label>{key}:</label>{option[key].options.map(this.makeCheckbox)}
            </div>)}
          </div>
        </form>
      </div>
    );
  }

  makeCheckbox(option) {
    return (
      <div className="checkbox">
        <label>
          <input type="checkbox" id="checkbox" value={option.value}/>
          {option.name ? option.name : option.value }
        </label>
      </div>
    );
  }

  render() {
    return (
      <div className="tile--config__modal fade in" id={`config-${this.props.tile.id}`} tabindex="-1"
           role="dialog"
           aria-labelledby="myModalLabel">
        <div className="tile--config__modal_dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <button type="button" className="close" data-dismiss="modal"
                      aria-label="Close"><span
                aria-hidden="true">&times;</span></button>
              <h4 className="modal-title" id={`title-${this.props.tile.id}`}>Change settings
                for {this.props.tile.title}</h4>
            </div>
            <div className="tile--config__modal_body">
              { this.makeFormBody() }
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-default" data-dismiss="modal">Close
              </button>
              <button type="button" className="btn btn-primary">Save changes</button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
