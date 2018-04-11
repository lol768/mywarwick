// @flow
import React from 'react';

type Props = {
  fw: boolean,
  size: string
};

const create = key => (props: Props) =>
  <i className={`fa fa-${key}${props.fw ? ' fa-fw' : ''}${props.size ? ` fa-${props.size}` : ''}`} />;

export const Clock = create('clock-o');
export const Map = create('map-marker');
export const User = create('user-o');
export const ChevronRight = create('chevron-right');
export const ChevronDown = create('chevron-down');
export const Calendar = create('calendar');
export const Mute = create('bell-slash-o');
