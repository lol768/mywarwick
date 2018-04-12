import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import SwitchListGroupItem from '../../../ui/SwitchListGroupItem';

export default class LocationOptInSettingsView extends React.PureComponent {
  static propTypes = {
    options: PropTypes.arrayOf(PropTypes.shape({
      value: PropTypes.string.isRequired,
      description: PropTypes.string.isRequired,
    })),
    selected: PropTypes.arrayOf(PropTypes.string),
    onChange: PropTypes.func.isRequired,
    disabled: PropTypes.bool.isRequired,
  };

  static defaultProps = {
    options: [],
    selected: [],
  };


  static buildState(options, selected) {
    return _.mapValues(
      _.keyBy(options, v => v.value),
      (v, key) => _.includes(selected, key),
    );
  }

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);

    this.state = LocationOptInSettingsView.buildState(this.props.options, this.props.selected);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(LocationOptInSettingsView.buildState(nextProps.options, nextProps.selected));
  }

  onClick(value) {
    if (this.props.disabled) return;
    const checked = !this.state[value];
    const newState = Object.assign({}, this.state, { [value]: checked });
    this.setState(newState);
    this.props.onChange('Location', _.keys(_.pickBy(newState, v => v)));
  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-1">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Location preferences</h3>
            </div>
          </div>
        </div>

        <p className="text--hint container-fluid">
          If you want, we can send you news and notifications specific to where you live.
          Choose any of the following if you&apos;d like news about those areas
          (or just leave them all unchecked if not).
        </p>

        <div className="list-group setting-colour-1">
          { _.map(this.props.options, location =>
            (<SwitchListGroupItem
              key={ `OptIn:Locations:${location.value}` }
              icon="map-signs"
              description={ location.description }
              value={ location.value }
              onClick={ this.onClick }
              checked={ this.state[location.value] }
              id={ `OptIn:Locations:${location.value}` }
              disabled={ this.props.disabled }
            />),
          ) }
        </div>
      </div>
    );
  }
}
