import React from 'react';
import * as PropTypes from 'prop-types';
import { Routes } from '../../AppRoot';
import ScrollRestore from '../../ui/ScrollRestore';
import LocationOptInSettingView from './optInSettings/LocationOptInSettingView';
import HideableView from '../HideableView';
import * as newsOptIn from '../../../state/news-optin';
import { connect } from 'react-redux';
import { replace } from 'react-router-redux';

class OptInSettingsView extends HideableView {

  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    options: PropTypes.objectOf(PropTypes.arrayOf(PropTypes.shape({
      value: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired,
    }))),
    selected: PropTypes.objectOf(PropTypes.arrayOf(PropTypes.string)),
    singleOptionView: PropTypes.func,
    singleOptionIdentifier: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.onChange = this.onChange.bind(this);
    this.onFinish = this.onFinish.bind(this);
  }

  componentDidShow() {
    this.props.dispatch(newsOptIn.fetch());
  }

  onChange(optinType, values) {
    this.props.dispatch(newsOptIn.persist(optinType, values));
  }

  onFinish() {
    this.props.dispatch(replace('/'));
  }

  render() {
    return (
      <ScrollRestore url={`/${Routes.SETTINGS}/${Routes.SettingsRoutes.OPT_IN}`} forceTop>
        <form>
          { (this.props.singleOptionView) ?
            React.createElement(
              this.props.singleOptionView,
              {
                options: this.props.options[this.props.singleOptionIdentifier] || [],
                selected: this.props.selected[this.props.singleOptionIdentifier] || [],
                onChange: this.onChange,
              }
            ) :
            <div>
              <LocationOptInSettingView options={ this.props.options.Location || [] }
                selected={ this.props.selected.Location || [] } onChange={ this.onChange }
              />

              <div className="container">
                <a href="#" className="btn btn-default btn-lg btn-block" onClick={ this.onFinish }>
                  Finish
                </a>
              </div>
            </div>
          }
        </form>
      </ScrollRestore>
    );
  }

}

const select = (state) => ({
  options: state.newsOptIn.options,
  selected: state.newsOptIn.selected,
});

export default connect(select)(OptInSettingsView);
