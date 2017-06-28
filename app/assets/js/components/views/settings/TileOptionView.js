import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import * as tiles from '../../../state/tiles';
import CheckboxListGroupItem from '../../ui/CheckboxListGroupItem';
import RadioListGroupItem from '../../ui/RadioListGroupItem';

export default class TileOptionView extends React.PureComponent {

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
    _.keys(props.tileOptions).forEach((key) => (currentPreferences[key] = undefined));

    this.state = {
      currentPreferences: {
        ...currentPreferences,
        ...props.tile.preferences,
      },
    };

    this.saveConfig = this.saveConfig.bind(this);
    this.onCheckboxClick = this.onCheckboxClick.bind(this);
    this.onRadioClick = this.onRadioClick.bind(this);
  }

  makeCheckboxItem(possibleChoice, cbName) {
    const currentPreference = this.state.currentPreferences[cbName];
    const { tile, tileOptions } = this.props;

    let checked = null;
    if (currentPreference !== undefined) {
      if (_.isArray(currentPreference)) {
        // NEWSTART-681 We can't use the defaults if it's an array
        // Eventually all of these will go away
        checked = currentPreference.indexOf(possibleChoice.value) !== -1;
      } else if (currentPreference[possibleChoice.value] !== undefined) {
        checked = currentPreference[possibleChoice.value];
      } else {
        checked = tileOptions[cbName].default.indexOf(possibleChoice.value) !== -1;
      }
    } else {
      checked = tileOptions[cbName].default.indexOf(possibleChoice.value) !== -1;
    }

    return (
      <CheckboxListGroupItem key={ `${cbName}:${possibleChoice.value}` }
        id={ `${cbName}:${possibleChoice.value}` } value={ possibleChoice.value }
        icon={ (tile.id === 'weather') ? 'sun-o' : tile.icon }
        description={ possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        onClick={ this.onCheckboxClick } checked={ checked } name={ cbName }
        disabled={ !this.props.isOnline }
      />
    );
  }

  makeRadioItem(possibleChoice, radioName) {
    const currentPreference = this.state.currentPreferences[radioName];
    const { tile, tileOptions } = this.props;

    let checked = null;
    if (currentPreference !== undefined && currentPreference.length > 0) {
      checked = currentPreference === possibleChoice.value;
    } else {
      checked = tileOptions[radioName].default === possibleChoice.value;
    }

    return (
      <RadioListGroupItem key={ `${radioName}:${possibleChoice.value}` }
        icon={ (tile.id === 'weather') ? 'sun-o' : tile.icon }
        description={ possibleChoice.name ? possibleChoice.name : possibleChoice.value }
        onClick={ this.onRadioClick } checked={ checked } name={ radioName }
        value={ possibleChoice.value }
        disabled={ !this.props.isOnline }
      />
    );
  }

  onCheckboxClick(value, name) {
    const currentPref = _.clone(this.state.currentPreferences, true);

    if (currentPref[name] === undefined) {
      currentPref[name] = { [value]: true };
    } else {
      if (_.isArray(currentPref[name])) {
        currentPref[name] = _.keyBy(currentPref[name]);
        _.keys(currentPref[name]).forEach((key) => (currentPref[name][key] = true));
      }
      currentPref[name][value] = !currentPref[name][value];
    }

    this.setState({ currentPreferences: currentPref });
    this.saveConfig(currentPref);
  }

  onRadioClick(value, name) {
    const currentPref = _.clone(this.state.currentPreferences, true);

    const newPreferences = {
      ...currentPref,
      [name]: value,
    };
    this.setState({ currentPreferences: newPreferences });
    this.saveConfig(newPreferences);
  }

  saveConfig(currentPreferences) {
    this.props.dispatch(tiles.saveTilePreferences(this.props.tile, currentPreferences));
  }

  render() {
    return (
      <div>
        <div className={ `list-group setting-colour-${this.props.tile.colour}` }>
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>{ `${this.props.tile.title} tile preferences` }</h3>
            </div>
          </div>
        </div>

        { _.flatMap(this.props.tileOptions, (tileOption, key) =>
          <div key={ key }>
            <p className="hint-text container-fluid">
              { tileOption.description }
            </p>
            <div key={ key } className={ `list-group setting-colour-${this.props.tile.colour}` }>
              { _.map(_.sortBy(tileOption.options, o => (o.name ? o.name : o.value)), option => {
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
          </div>
        ) }
      </div>
    );
  }
}
