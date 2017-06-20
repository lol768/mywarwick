import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import * as tiles from '../../../state/tiles';

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
    this.handleCheckboxItemClick = this.handleCheckboxItemClick.bind(this);
    this.handleRadioItemClick = this.handleRadioItemClick.bind(this);
    this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
    this.handleRadioChange = this.handleRadioChange.bind(this);
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
      <div key={ `${cbName}:${possibleChoice.value}` } className="list-group-item"
        data-name={cbName} data-value={possibleChoice.value}
        onClick={ this.handleCheckboxItemClick }
      >
        <div className="media">
          <div className="media-left feedback">
            <i className={ `fa fa-fw fa-${
                (tile.id === 'weather') ? 'sun-o' : tile.icon
              }` }
            />
          </div>
          <div className="media-body">
            {possibleChoice.name ? possibleChoice.name : possibleChoice.value }
          </div>
          <div className="media-right">
            <input
              type="checkbox"
              data-value={possibleChoice.value}
              data-name={cbName}
              checked={checked}
              onChange={ this.handleCheckboxChange }
            />
          </div>
        </div>
      </div>
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
      <div key={ `${radioName}:${possibleChoice.value}` } data-name={radioName}
        data-value={possibleChoice.value} className="list-group-item"
        onClick={ this.handleRadioItemClick }
      >
        <div className="media">
          <div className="media-left feedback">
            <i className={ `fa fa-fw fa-${
              (tile.id === 'weather') ? 'sun-o' : tile.icon
              }` }
            />
          </div>
          <div className="media-body">
            {possibleChoice.name ? possibleChoice.name : possibleChoice.value }
          </div>
          <div className="media-right">
            <input
              type="radio"
              data-name={radioName}
              data-value={possibleChoice.value}
              checked={checked}
              onChange={ this.handleRadioChange }
            />
          </div>
        </div>
      </div>
    );
  }

  handleRadioItemClick(event) {
    const value = event.currentTarget.dataset.value;
    const name = event.currentTarget.dataset.name;
    const currentPref = _.clone(this.state.currentPreferences, true);

    const newPreferences = {
      ...currentPref,
      [name]: value,
    };
    this.setState({
      currentPreferences: newPreferences,
    });
    this.saveConfig(newPreferences);
  }

  handleRadioChange(event) {
    event.stopPropagation();
    this.handleRadioItemClick(event);
  }

  handleCheckboxItemClick(event) {
    const value = event.currentTarget.dataset.value;
    const name = event.currentTarget.dataset.name;
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

  handleCheckboxChange(event) {
    event.stopPropagation();
    this.handleCheckboxItemClick(event);
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
          <div key={ key } className={ `list-group setting-colour-${this.props.tile.colour}` }>
            <div className="list-group-item list-group-item--header">
              { tileOption.description }
            </div>
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
        ) }
      </div>
    );
  }
}
