// @flow
import React from 'react';

type Props = {
  fw: boolean
};

const create = key => (props: Props) => <i className={`fa fa-${key}${props.fw ? ' fa-fw' : ''}`} />;

export const Clock = create('clock-o');
export const Map = create('map-marker');
export const User = create('user-o');
export const ChevronRight = create('chevron-right');
export const Calendar = create('calendar');
export const Info = create('info-circle');
