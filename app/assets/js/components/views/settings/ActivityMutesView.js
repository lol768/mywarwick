import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash-es';
import * as PropTypes from 'prop-types';
import * as dateFormats from '../../../dateFormats';
import * as notifications from '../../../state/notifications';
import ScrollRestore from '../../ui/ScrollRestore';
import { Routes } from '../../AppRoot';
import EmptyState from '../../ui/EmptyState';
import wrapKeyboardSelect from '../../../keyboard-nav';
import { Mute } from '../../FA';

const activityMuteType = PropTypes.shape({
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
});

// Should handle this generally in the date format functions -
// maybe they should always format text for the middle of a sentence,
// as it's easier to capitalise the first letter afterward than to do
// the reverse.
function handleDateCase(formattedDate) {
  return formattedDate.replace('Tomorrow', 'tomorrow');
}

export class ActivityMutesView extends React.PureComponent {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    activityMutes: PropTypes.arrayOf(activityMuteType).isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);
    this.onDelete = this.onDelete.bind(this);
  }

  onDelete(e, mute) {
    wrapKeyboardSelect(() => this.props.dispatch(notifications.deleteActivityMute(mute)), e);
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
                          `Mute until ${handleDateCase(dateFormats.forActivity(mute.expiresAt))}`
                          : 'Mute until removed'
                      }</div>
                      <div className="activity-item__text">
                        <MuteDescription mute={mute} />
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
                        onClick={ e => this.onDelete(e, mute) }
                        onKeyUp={ e => this.onDelete(e, mute) }
                        disabled={ !this.props.isOnline }
                      >
                        <i className="fa fa-times" />
                      </button>
                    </div>
                  </div>
                </div>),
              )
              : <EmptyState>
                  You haven&apos;t muted any alerts. Use the <Mute fw /> icon next to each
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

export class MuteDescription extends React.PureComponent {
  static propTypes = {
    mute: activityMuteType
  };

  render() {
    const mute = this.props.mute;

    const tagsEmpty = _.isEmpty(mute.tags);
    const providerOnly = mute.provider && !mute.activityType && tagsEmpty;

    const typeName = () => mute.activityType.displayName || mute.activityType.name;
    const providerName = () => mute.provider.displayName || mute.provider.id;

    if (mute.provider && mute.activityType && tagsEmpty) {
      // Display as a single item
      return <ul>
        <li>'{typeName()}' alerts from {providerName()}</li>
      </ul>;
    } else {
      return (
        <ul>
          {mute.activityType && <li>'{typeName()}' alerts</li>}
          {mute.provider &&
            <li>{providerOnly ? 'All ' : null}{providerName()} alerts</li>
          }
          {
            _.map(mute.tags, tag =>
              <li key={`${mute.id}-tag-${tag.name}-${tag.value}`}>
                {tag.display_value || tag.value}
              </li>
            )
          }
        </ul>
      );
    }
  }
}

function select(state) {
  return {
    activityMutes: state.notifications.activityMutes,
    isOnline: state.device.isOnline,
  };
}

export default connect(select)(ActivityMutesView);
