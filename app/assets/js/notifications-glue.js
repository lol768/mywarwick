import localforage from 'localforage';
import moment from 'moment';
import { createSelector } from 'reselect';

const notificationsLastReadSelector = state => state.get('notifications-lastRead');
const userSelector = state => state.getIn(['user', 'data']);

export const persistNotificationsMetadata =
  createSelector([notificationsLastReadSelector, userSelector], (lastRead, user) => {
    if (lastRead && user) {
      localforage.getItem('notifications-lastRead').then(lastReadLocal => {
        if (lastReadLocal === null || moment(lastReadLocal).isBefore(lastRead)) {
          fetch('/api/streams/read', {
            method: 'post',
            headers: {
              Accept: 'application/json',
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              usercode: user.get('usercode'),
              notificationsRead: lastRead.format(),
            }),
            credentials: 'same-origin',
          });
        }
      });
    }
  });
