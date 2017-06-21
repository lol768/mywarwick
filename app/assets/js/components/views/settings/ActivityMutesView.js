import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash-es';
import * as dateFormats from '../../../dateFormats';
import * as notifications from '../../../state/notifications';
import ScrollRestore from '../../ui/ScrollRestore';
import EmptyState from '../../ui/EmptyState';
import { Routes } from '../../AppRoot';
import * as PropTypes from 'prop-types';

class ActivityMutesView extends React.PureComponent {

  static propTypes = {
    dispatch: React.PropTypes.func.isRequired,
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
          <div className="list-group setting-colour-0">
            <div className="list-group-item">
              <div className="list-group-item-heading">
                <h3>Muted notifications</h3>
              </div>
            </div>
          </div>
          {
            this.props.activityMutes.length > 0 ?
              _.map(this.props.activityMutes, (mute) =>
                <div className="activity-item" key={ mute.id }>
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
                              <li key={ `${mute.id}-tag-${tag.name}-${tag.value}` }>
                                { tag.display_value || tag.value }
                              </li>
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
                      >
                        <i className="fa fa-times" />
                      </button>
                    </div>
                  </div>
                </div>
              )
              : <EmptyState>
                  You haven't muted any notifications. Use the dropdown arrow next to each
                  notification to specify similar types of notifications to mute in future.
                  Muted notifications still appear in the list of notifications
                  ; they just don't pop up on your device.
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
  };
}

export default connect(select)(ActivityMutesView);