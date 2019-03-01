import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import * as notifications from '../../../state/notifications';
import Switch from '../../ui/Switch';
import wrapKeyboardSelect from '../../../keyboard-nav';
import $ from 'jquery';
import { Mute } from '../../FA';

class StreamFilterOptionView extends React.PureComponent {
  static propTypes = {
    filterType: PropTypes.string.isRequired,
    filter: PropTypes.object.isRequired,
    filterOptions: PropTypes.shape({
      provider: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired,
        displayName: PropTypes.string,
        icon: PropTypes.string,
        colour: PropTypes.string,
        overrideMuting: PropTypes.bool,
      })).isRequired,
    }).isRequired,
    saveFilter: PropTypes.func.isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  static defaultProps = {
    filterOptions: {
      provider: [],
    },
  };

  static buildState(filterOptions, filter) {
    const updatedState = {
      provider: {},
    };
    _.forEach(filterOptions.provider, (p) => {
      updatedState.provider[p.id] = (
        filter.provider === undefined ||
        filter.provider[p.id] === undefined ||
        filter.provider[p.id]
      );
    });
    return updatedState;
  }

  constructor(props) {
    super(props);

    this.state = StreamFilterOptionView.buildState(this.props.filterOptions, this.props.filter);
    this.onClick = this.onClick.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(StreamFilterOptionView.buildState(nextProps.filterOptions, nextProps.filter));
  }

  onClick(event) {
    wrapKeyboardSelect((e) => {
      const dataset = $(e.currentTarget).data();
      if (dataset.disabled) return;
      const value = dataset.value;
      const name = dataset.name;
      const newOption = _.cloneDeep(this.state[name]);
      newOption[value] = !newOption[value];

      this.setState({ [name]: newOption }, () => this.props.saveFilter(this.state));
    }, event);
  }

  renderProvider() {
    const providers = this.props.filterOptions.provider;
    const plural = (this.props.filterType === 'Alerts') ? 'Alerts' : 'Activities';

    if (providers.length === 0) {
      return (
        <div className="empty-state">
          You haven’t recorded any { plural.toLowerCase() } yet. When you do, you’ll be
          able to use this screen to choose which types
          of { this.props.filterType.toLowerCase() } you’d like to see on
          your { this.props.filterType } tab.
        </div>
      );
    }

    return (
      <div>
        {plural === 'Alerts' &&
        <p className="text--hint container-fluid">
          Filtering alerts will not stop them playing a sound or appearing on
          your phone’s lock screen. To stop this you
          should use the <Mute fw /> icon next to each alert in the Alerts tab.
        </p>
        }
        <p className="text--hint container-fluid">
          On my { plural } tab, show { plural.toLowerCase() } that come from
        </p>
        <div className="list-group">
          { _.map(
            _.sortBy(providers, o => (o.displayName ? o.displayName : o.name)),
            option =>
              (<div
                key={ `provider:${option.id}` }
                className="list-group-item"
                data-name="provider"
                data-value={option.id}
                data-disabled={!this.props.isOnline || option.overrideMuting}
                role="button"
                tabIndex={0}
                onClick={ this.onClick }
                onKeyUp={ this.onClick }
              >
                <div className="media">
                  <div className="media-left">
                    <i
                      className={ `fa fa-fw fa-${option.icon ? option.icon : 'cog'}` }
                      style={{ color: (option.colour ? option.colour : 'black') }}
                    />
                  </div>
                  <div
                    className={`media-body${this.props.isOnline ? '' : ' media-body-disabled'}`}
                  >
                    { option.displayName ? option.displayName : option.name }
                  </div>
                  <div className="media-right">
                    <Switch
                      id={ `${this.props.filterType}:provider:${option.id}` }
                      checked={ this.state.provider[option.id] }
                      disabled={ !this.props.isOnline || option.overrideMuting }
                    />
                  </div>
                </div>
              </div>),
          ) }
        </div>
      </div>
    );
  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-2">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>{ `${this.props.filterType} filter` }</h3>
            </div>
          </div>
        </div>

        { this.renderProvider() }
      </div>
    );
  }
}

function selectActivity(state) {
  return {
    filterType: 'Activity',
    filter: state.activities.filter,
    filterOptions: state.activities.filterOptions,
    isOnline: state.device.isOnline,
  };
}

function selectNotification(state) {
  return {
    filterType: 'Alerts',
    filter: state.notifications.filter,
    filterOptions: state.notifications.filterOptions,
    isOnline: state.device.isOnline,
  };
}

export const ActivityStreamFilterOptionView =
  connect(
    selectActivity,
    dispatch => ({
      saveFilter: thisState => dispatch(notifications.persistActivityFilter(thisState)),
    }),
  )(StreamFilterOptionView);

export const NotificationStreamFilterOptionView =
  connect(
    selectNotification,
    dispatch => ({
      saveFilter: thisState => dispatch(notifications.persistNotificationFilter(thisState)),
    }),
  )(StreamFilterOptionView);
