import React from 'react';
import HideableView from './HideableView';
import OptInSettingsView from './settings/OptInSettingsView';
import { replace } from 'react-router-redux';
import { connect } from 'react-redux';

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
          <a href="#" className="btn btn-default btn-lg btn-block" onClick={ this.onFinish }>
            Finish
          </a>
        </div>
      </div>
    );
  }

}

export default connect()(PostTourView);
