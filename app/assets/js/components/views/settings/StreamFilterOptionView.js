import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import * as notifications from '../../../state/notifications';
import { connect } from 'react-redux';

class StreamFilterOptionView extends React.PureComponent {

  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    filterType: PropTypes.string.isRequired,
    filter: PropTypes.object.isRequired,
    filterOptions: PropTypes.shape({
      provider: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string.isRequired,
        displayName: PropTypes.string,
        icon: PropTypes.string,
        colour: PropTypes.string,
      })).isRequired,
    }).isRequired,
    saveFilter: PropTypes.func.isRequired,
  };

  static defaultProps = {
    filterOptions: {
      provider: [],
    },
  };

  constructor(props) {
    super(props);

    this.state = this.buildState(props);

    this.handleItemClick = this.handleItemClick.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(this.buildState(nextProps));
  }

  buildState(props) {
    const updatedState = {
      provider: {},
    };
    _.forEach(props.filterOptions.provider, p => {
      updatedState.provider[p.id] = (
        props.filter.provider === undefined ||
        props.filter.provider[p.id] === undefined ||
        props.filter.provider[p.id]
      );
    });
    return updatedState;
  }

  handleItemClick(event) {
    const value = event.currentTarget.dataset.value;
    const name = event.currentTarget.dataset.name;
    const newOption = _.cloneDeep(this.state[name]);
    newOption[value] = !newOption[value];

    this.setState({ [name]: newOption }, () => this.props.saveFilter(this.state));
  }

  handleChange(event) {
    event.stopPropagation();
    this.handleItemClick(event);
  }

  render() {
    const { filterOptions } = this.props;

    return (
      <div>
        <div className="list-group setting-colour-2">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>{ `${this.props.filterType} filter` }</h3>
            </div>
          </div>
        </div>

        <div className="list-group">
            <div className="list-group-item list-group-item--header">
              Provider
            </div>
            { _.map(
              _.sortBy(filterOptions.provider, o => (o.displayName ? o.displayName : o.name)),
              option =>
                <div key={ `provider:${option.id}` } className="list-group-item"
                  data-name="provider" data-value={option.id}
                  onClick={ this.handleItemClick }
                >
                  <div className="media">
                    <div className="media-left feedback">
                      <i className={ `fa fa-fw fa-${option.icon ? option.icon : 'cog'}` }
                        style={{ color: (option.colour ? option.colour : 'black') }}
                      />
                    </div>
                    <div className="media-body">
                      { option.displayName ? option.displayName : option.name }
                    </div>
                    <div className="media-right">
                      <input
                        type="checkbox"
                        data-value={option.id}
                        data-name="provider"
                        checked={ this.state.provider[option.id] }
                        onChange={ this.handleChange }
                      />
                    </div>
                  </div>
                </div>
            ) }
        </div>
      </div>
    );
  }
}

function selectActivity(state) {
  return {
    filterType: 'Activity',
    filter: state.activities.filter,
    filterOptions: state.activities.filterOptions,
  };
}

function selectNotification(state) {
  return {
    filterType: 'Notifications',
    filter: state.notifications.filter,
    filterOptions: state.notifications.filterOptions,
  };
}

export const ActivityStreamFilterOptionView =
  connect(
    selectActivity,
    dispatch => ({
      saveFilter: (thisState) => dispatch(notifications.persistActivityFilter(thisState)),
    })
  )(StreamFilterOptionView);

export const NotificationStreamFilterOptionView =
  connect(
    selectNotification,
    dispatch => ({
      saveFilter: (thisState) => dispatch(notifications.persistNotificationFilter(thisState)),
    })
  )(StreamFilterOptionView);
