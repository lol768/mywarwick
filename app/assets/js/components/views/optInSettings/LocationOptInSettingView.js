import React, { Component } from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';

export default class LocationOptInSettingsView extends Component {

  static propTypes = {
    options: PropTypes.arrayOf(PropTypes.shape({
      value: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired,
    })),
    selected: PropTypes.arrayOf(PropTypes.string),
    onChange: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.renderOption = this.renderOption.bind(this);
    this.onCheckboxChange = this.onCheckboxChange.bind(this);

    this.state = this.buildState(props);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(this.buildState(nextProps));
  }

  onCheckboxChange(event) {
    const target = event.target;
    const value = target.value;
    const checked = target.checked;

    this.setState({ [value]: checked });
    this.props.onChange();
  }

  buildState(props) {
    return _.mapValues(
      _.keyBy(props.options, v => v.value),
      (v, key) => _.includes(props.selected, key)
    );
  }

  renderOption(location) {
    return (
      <div key={ `OptIn:Locations:${location.value}` } className="checkbox">
        <label>
          <input type="checkbox" name="Location" value={ location.value }
            checked={ this.state[location.value] } onChange={ this.onCheckboxChange }
          />
          { location.description }
        </label>
      </div>
    );
  }

  render() {
    return (
      <div className="container-fluid margin-top-1">
        <h3>Location preferences</h3>
        <fieldset>
          <p>
            If you want, we can send you news and notifications specific to where you live.
            Choose any of the following if you'd like news about those areas
            (or just leave them all unchecked if not).
          </p>
          { _.map(this.props.options, this.renderOption) }
        </fieldset>
      </div>
    );
  }

}
