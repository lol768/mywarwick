import React from 'react';
import { replace } from 'react-router-redux';
import { connect } from 'react-redux';
import HideableView from './HideableView';
import OptInSettingsView from './settings/OptInSettingsView';
import wrapKeyboardSelect from '../../keyboard-nav';

class PostTourView extends HideableView {
  constructor(props) {
    super(props);
    this.onFinish = this.onFinish.bind(this);
  }

  onFinish(e) {
    wrapKeyboardSelect(() => this.props.dispatch(replace('/')), e);
  }

  render() {
    return (
      <div>
        <OptInSettingsView />

        <div className="container">
          <a
            className="btn btn-default btn-lg btn-block"
            onClick={ this.onFinish }
            onKeyUp={ this.onFinish }
            role="button"
            tabIndex={0}
          >
            Finish
          </a>
        </div>
      </div>
    );
  }
}

export default connect()(PostTourView);
