import React, { PropTypes } from 'react';

import _ from 'lodash';

function icon(text) {
  const values = text.split(' ', 2);

  return {
    color: values[0],
    icon: values[1],
  };
}

const COLOURS = {
  green: '#59b399',
  blue: '#00b2dd',
  orange: '#f47920',
  red: '#ef4050',
  tabula: '#239b92',
  its: '#156297',
  coursesync: '#0081c2',
  moodle: '#55b5eb',
  servicenow: '#c52129',
  greenBlue: '#32adb2',
  outlook: '#0078d7',
  mahara: '#789b3b',
  id7default: '#8c6e96',
};

const ICONS = _({
  Photos: 'greeAppIconn camera',
  tabula: 'tabula cog',
  'Web Sign-On': 'orange lock',
  library: 'orange book',
  'sports-centre': 'blue futbol-o',
  'IT Services': 'servicenow question-circle',
  hearnow: 'orange pie-chart',
  Printing: 'its print',
  eating: 'green cutlery',
  SiteBuilder: 'red globe',
  BlogBuilder: 'green globe',
  Mahara: 'mahara mortar-board',
  Moodle: 'moodle mortar-board',
  Update: 'id7default arrow-up',
}).mapValues(icon).value();

const AppIcon = (props) => {
  const app = ICONS[props.app] || icon('red exclamation-triangle');
  const sizeClass = props.size ? `app-icon--${props.size}` : '';

  return (
    <i
      className={ `app-icon ${sizeClass} fa fa-fw fa-${app.icon}` }
      style={{ backgroundColor: COLOURS[app.color], color: 'white' }}
    > </i>
  );
};

AppIcon.propTypes = {
  app: PropTypes.string,
  size: PropTypes.int,
};

export default AppIcon;
