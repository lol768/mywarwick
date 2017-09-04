import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import * as tiles from '../../../state/tiles';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import RadioListGroupItem from '../../ui/RadioListGroupItem';

export class TileOptionView extends React.PureComponent {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    tile: PropTypes.shape({
      id: PropTypes.string.isRequired,
      colour: PropTypes.number.isRequired,
      icon: PropTypes.string.isRequired,
      title: PropTypes.string.isRequired,
      preferences: PropTypes.object,
    }).isRequired,
    tileOptions: PropTypes.object.isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);

    const currentPreferences = {};
    _.keys(props.tileOptions).forEach((key) => {
      const tileOption = props.tileOptions[key];

      // Populate the options
      if (tileOption.type === 'array') {
        currentPreferences[key] = {};
        tileOption.options.forEach(option => (currentPreferences[key][option.value] = false));
      } else {
        currentPreferences[key] = '';
      }

      // Load the defaults
      if (tileOption.type === 'array') {
        tileOption.default.forEach(value => (currentPreferences[key][value] = true));
      } else {
        currentPreferences[key] = tileOption.default;
      }

      // Overwrite with user preference if defined
      if (props.tile.preferences !== null && props.tile.preferences[key] !== undefined) {
        if (tileOption.type === 'array') {
          _.keys(props.tile.preferences[key]).forEach((option) => {
            currentPreferences[key][option] = props.tile.preferences[key][option];
          });
        } else {
          currentPreferences[key] = props.tile.preferences[key];
        }
      }
    });

    this.state = { currentPreferences };

    this.saveConfig = this.saveConfig.bind(this);
    this.saveTilePreferences = _.debounce(this.saveTilePreferences.bind(this), 1000);
    this.onCheckboxClick = this.onCheckboxClick.bind(this);
    this.onRadioClick = this.onRadioClick.bind(this);
  }

  onCheckboxClick(value, name) {
    const currentPref = _.clone(this.state.currentPreferences);
    currentPref[name][value] = !currentPref[name][value];

    this.saveConfig(currentPref);
  }

  onRadioClick(value, name) {
    const currentPref = _.clone(this.state.currentPreferences);

    const newPreferences = {
      ...currentPref,
      [name]: value,
    };

    this.saveConfig(newPreferences);
  }

  makeRadioItem(possibleChoice, groupName) {
    const currentPreference = this.state.currentPreferences[groupName];
    const { tile, tileOptions } = this.props;
    const checked = currentPreference === possibleChoice.value;

    return (
      <RadioListGroupItem
        key={ `${groupName}:${possibleChoice.value}` }
        icon={tileOptions[groupName].icon || tile.icon}
        description={ possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        onClick={ this.onRadioClick }
        checked={ checked }
        name={ groupName }
        value={ possibleChoice.value }
        disabled={ !this.props.isOnline }
      />
    );
  }

  makeCheckboxItem(possibleChoice, groupName) {
    const currentPreference = this.state.currentPreferences[groupName];
    const { tile, tileOptions } = this.props;

    const checked = currentPreference[possibleChoice.value];

    return (
      <SwitchListGroupItem
        key={ `${groupName}:${possibleChoice.value}` }
        id={ `${groupName}:${possibleChoice.value}` }
        value={ possibleChoice.value }
        icon={tileOptions[groupName].icon || tile.icon}
        description={ possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        onClick={ this.onCheckboxClick }
        checked={ checked }
        name={ groupName }
        disabled={ !this.props.isOnline }
      />
    );
  }

  saveTilePreferences() {
    this.props.dispatch(tiles.saveTilePreferences(this.props.tile, this.state.currentPreferences));
  }

  saveConfig(currentPreferences) {
    this.setState({ currentPreferences }, this.saveTilePreferences);
  }

  render() {
    return (
      <div>
        <div className={ `list-group fixed setting-colour-${this.props.tile.colour}` }>
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>{ `${this.props.tile.title} tile preferences` }</h3>
            </div>
          </div>
        </div>

        { _.flatMap(this.props.tileOptions, (tileOption, key) =>
          (<div key={ key }>
            <p className="hint-text container-fluid">
              { tileOption.description }
            </p>
            <div key={ key } className={ `list-group setting-colour-${this.props.tile.colour}` }>
              { _.map(_.sortBy(tileOption.options, o => (o.name ? o.name : o.value)), (option) => {
                switch (tileOption.type.toLowerCase()) {
                  case 'array':
                    return this.makeCheckboxItem(option, key);
                  case 'string':
                    return this.makeRadioItem(option, key);
                  default:
                    return (
                      <div />
                    );
                }
              }) }
            </div>
          </div>),
        ) }
      </div>
    );
  }
}

const select = state => ({
  isOnline: state.device.isOnline,
});

export default connect(select)(TileOptionView);
