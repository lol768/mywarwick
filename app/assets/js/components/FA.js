// @flow
import React from 'react';

type Props = {
  fw: boolean,
  size: string
};

// eslint-disable-next-line react/display-name
const create = key => (props: Props) => <i className={`fal fa-${key}${props.fw ? ' fa-fw' : ''}${props.size ? ` fa-${props.size}` : ''}`} />;

export const Clock = create('clock');
Clock.displayName = 'Clock';

export const Map = create('map-marker-alt');
Map.displayName = 'Map';

export const User = create('user');
User.displayName = 'User';

export const ChevronRight = create('chevron-right');
ChevronRight.displayName = 'ChevronRight';

export const ChevronDown = create('chevron-down');
ChevronDown.displayName = 'ChevronDown';

export const Calendar = create('calendar-alt');
Calendar.displayName = 'Calendar';

export const Mute = create('bell-slash');
Mute.displayName = 'Mute';

export const Info = create('info-circle');
Info.displayName = 'Info';

export const Commenting = create('comment-dots');
Commenting.displayName = 'Commenting';
