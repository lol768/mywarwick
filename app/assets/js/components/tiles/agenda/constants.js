import * as PropTypes from 'prop-types';

export const eventShape = {
  id: PropTypes.string,
  start: PropTypes.string,
  end: PropTypes.string,
  isAllDay: PropTypes.bool,
  title: PropTypes.string,
  organiser: PropTypes.shape({
    name: PropTypes.string,
  }),
  location: PropTypes.shape({
    name: PropTypes.string,
    href: PropTypes.string,
  }),
  href: PropTypes.string,
  parent: PropTypes.shape({
    shortName: PropTypes.string,
    fullName: PropTypes.string,
  }),
  type: PropTypes.string,
  extraInfo: PropTypes.string,
  staff: PropTypes.arrayOf(PropTypes.shape({
    email: PropTypes.string,
    lastName: PropTypes.string,
    firstName: PropTypes.string,
    userType: PropTypes.string,
    universityId: PropTypes.string,
  })),
};

export const eventPropType = PropTypes.shape(eventShape);
