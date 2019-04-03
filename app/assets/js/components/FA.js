// @flow
import React from 'react';

type Props = {
  fw: boolean,
  size: string
};

const create = key => (props: Props) =>
  <i className={`fal fa-${key}${props.fw ? ' fa-fw' : ''}${props.size ? ` fa-${props.size}` : ''}`} />;

export const Clock = create('clock');
export const Map = create('map-marker-alt');
export const User = create('user');
export const ChevronRight = create('chevron-right');
export const ChevronDown = create('chevron-down');
export const Calendar = create('calendar-alt');
export const Mute = create('bell-slash');
export const Info = create('info-circle');
export const Commenting = create('comment-dots');
