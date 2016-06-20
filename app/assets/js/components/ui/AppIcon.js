import React, { PropTypes } from 'react';

const AppIcon = (props) => {
  const { size, icon: { name, colour } } = props;
  const sizeClass = size ? `app-icon--${size}` : '';
  return (
    <i
      className={ `app-icon ${sizeClass} fa fa-fw fa-${name}` }
      style={{ backgroundColor: `${colour}`, color: 'white' }}
    > </i>
  );
};

AppIcon.propTypes = {
  icon: React.PropTypes.shape({
    name: React.PropTypes.string,
    colour: React.PropTypes.string,
  }),
  colour: React.PropTypes.string,
  size: PropTypes.string,
};

AppIcon.defaultProps = {
  icon: {
    name: 'exclamation-triangle',
    colour: 'red',
  },
};

export default AppIcon;
