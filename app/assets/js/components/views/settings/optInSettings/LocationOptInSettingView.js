import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import CheckboxListGroupItem from '../../../ui/CheckboxListGroupItem';

export default class LocationOptInSettingsView extends React.PureComponent {

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
    this.onClick = this.onClick.bind(this);

    this.state = this.buildState(props);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(this.buildState(nextProps));
  }

  onClick(value) {
    const checked = !this.state[value];
    const newState = Object.assign({}, this.state, { [value]: checked });
    this.setState(newState);
    this.props.onChange('Location', _.keys(_.pickBy(newState, v => v)));
  }

  buildState(props) {
    return _.mapValues(
      _.keyBy(props.options, v => v.value),
      (v, key) => _.includes(props.selected, key)
    );
  }

  render() {
    return (
      <div>
        <div className="list-group setting-colour-1">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Location preferences</h3>
            </div>
          </div>
        </div>

        <p className="hint-text container-fluid">
          If you want, we can send you news and notifications specific to where you live.
          Choose any of the following if you'd like news about those areas
          (or just leave them all unchecked if not).
        </p>

        <div className="list-group setting-colour-1">
          { _.map(this.props.options, location =>
            <CheckboxListGroupItem key={ `OptIn:Locations:${location.value}` } icon="map-signs"
              description={ location.description } value={ location.value } onClick={ this.onClick }
              checked={ this.state[location.value] } id={ `OptIn:Locations:${location.value}` }
            />
          ) }
        </div>
      </div>
    );
  }

}
