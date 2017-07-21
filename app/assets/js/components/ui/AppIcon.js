import React from 'react';
import * as PropTypes from 'prop-types';

const AppIcon = (props) => {
  const { size, icon: { name, colour } } = props;
  const sizeClass = size ? `app-icon--${size}` : '';
  return (
    <i
      className={ `app-icon ${sizeClass} fa fa-fw fa-${name}` }
      style={{ backgroundColor: `${colour}`, color: 'white' }}
    />
  );
};

AppIcon.propTypes = {
  icon: PropTypes.shape({
    name: PropTypes.string,
    colour: PropTypes.string,
  }),
  colour: PropTypes.string,
  size: PropTypes.string,
};

AppIcon.defaultProps = {
  icon: {
    name: 'exclamation-triangle',
    colour: 'red',
  },
};

export default AppIcon;
