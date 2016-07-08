/**
 * Export all the reducers by their key names, so that you can import this
 * module and pass it straight to combineReducers.
 *
 * If we had all the reducers on their own in a reducers folder, exported as
 * defaults, then we'd be able to import the folder and not have to maintain
 * this file.
 */

export { reducer as news } from './news';
export { reducer as notificationsLastRead } from './notification-metadata';
export { notificationsReducer as notifications } from './notifications';
export { activitiesReducer as activities } from './notifications';
export { tilesReducer as tiles } from './tiles';
export { tileContentReducer as tileContent } from './tiles';
export { reducer as update } from './update';
export { reducer as user } from './user';
export { reducer as ui } from './ui';
export { reducer as device } from './device';
