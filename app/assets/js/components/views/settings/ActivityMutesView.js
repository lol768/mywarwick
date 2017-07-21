import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash-es';
import * as PropTypes from 'prop-types';
import * as dateFormats from '../../../dateFormats';
import * as notifications from '../../../state/notifications';
import ScrollRestore from '../../ui/ScrollRestore';
import { Routes } from '../../AppRoot';
import EmptyState from '../../ui/EmptyState';

class ActivityMutesView extends React.PureComponent {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    activityMutes: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string.isRequired,
      usercode: PropTypes.string.isRequired,
      createdAt: PropTypes.string.isRequired,
      expiresAt: PropTypes.string,
      activityType: PropTypes.shape({
        name: PropTypes.string.isRequired,
        displayName: PropTypes.string,
      }),
      provider: PropTypes.shape({
        id: PropTypes.string.isRequired,
        displayName: PropTypes.string,
      }),
      tags: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
        display_name: PropTypes.string,
        value: PropTypes.string.isRequired,
        display_value: PropTypes.string,
      })),
    })).isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);
    this.onDelete = this.onDelete.bind(this);
  }

  onDelete(mute) {
    this.props.dispatch(notifications.deleteActivityMute(mute));
  }

  render() {
    return (
      <ScrollRestore
        url={`/${Routes.SETTINGS}/${Routes.SettingsRoutes.MUTES}`}
        forceTop
      >
        <div>
          <div className="list-group fixed setting-colour-0">
            <div className="list-group-item">
              <div className="list-group-item-heading">
                <h3>Muted alerts</h3>
              </div>
            </div>
          </div>
          {
            this.props.activityMutes.length > 0 ?
              _.map(this.props.activityMutes, mute =>
                (<div className="activity-item" key={ mute.id }>
                  <div className="media">
                    <div className="media-body">
                      <div className="activity-item__title">{
                        mute.expiresAt ?
                          `Mute until ${dateFormats.forActivity(mute.expiresAt)}`
                          : 'Mute until removed'
                      }</div>
                      <div className="activity-item__text">
                        <ul>
                          {
                            mute.activityType ?
                              <li>{
                                mute.activityType.displayName || mute.activityType.name
                              }</li> : null
                          }
                          {
                            mute.provider ?
                              <li>{
                                mute.provider.displayName || mute.provider.id
                              }</li> : null
                          }
                          {
                            _.map(mute.tags, tag =>
                              (<li key={ `${mute.id}-tag-${tag.name}-${tag.value}` }>
                                { tag.display_value || tag.value }
                              </li>),
                            )
                          }
                        </ul>
                      </div>
                      <div className="activity-item__date">
                        Created { dateFormats.forActivity(mute.createdAt) }
                      </div>
                    </div>
                    <div className="media-right media-middle">
                      <button
                        type="button"
                        className="btn btn-danger"
                        data-dismiss="modal"
                        onClick={ () => this.onDelete(mute) }
                        disabled={ !this.props.isOnline }
                      >
                        <i className="fa fa-times" />
                      </button>
                    </div>
                  </div>
                </div>),
              )
              : <EmptyState>
                  You haven&apos;t muted any alerts. Use the dropdown arrow next to each
                  alert to specify similar types of alerts to mute in future.
                  Muted alerts still appear in the list of alerts;
                  they just don&apos;t pop up on your device.
              </EmptyState>
          }
        </div>
      </ScrollRestore>
    );
  }
}

function select(state) {
  return {
    activityMutes: state.notifications.activityMutes,
    isOnline: state.device.isOnline,
  };
}

export default connect(select)(ActivityMutesView);
