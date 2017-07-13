import React from 'react';
import { replace } from 'react-router-redux';
import { connect } from 'react-redux';
import HideableView from './HideableView';
import OptInSettingsView from './settings/OptInSettingsView';

class PostTourView extends HideableView {
  constructor(props) {
    super(props);
    this.onFinish = this.onFinish.bind(this);
  }

  onFinish() {
    this.props.dispatch(replace('/'));
  }

  render() {
    return (
      <div>
        <OptInSettingsView />

        <div className="container">
          <a href="#finish-tour" className="btn btn-default btn-lg btn-block" onClick={ this.onFinish }>
            Finish
          </a>
        </div>
      </div>
    );
  }
}

export default connect()(PostTourView);
