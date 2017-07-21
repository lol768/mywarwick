import React from 'react';
import * as PropTypes from 'prop-types';
import ReactDOM from 'react-dom';

export default class AccountPhoto extends React.PureComponent {
  static propTypes = {
    user: PropTypes.shape({
      photo: PropTypes.shape({
        url: PropTypes.string,
      }),
      name: PropTypes.string.isRequired,
    }).isRequired,
    className: PropTypes.string,
  };

  constructor(props) {
    super(props);

    this.onPhotoError = this.onPhotoError.bind(this);
  }

  onPhotoError() {
    const photo = ReactDOM.findDOMNode(this.refs.photo);

    photo.src = '/assets/images/no-photo.png';
  }

  render() {
    const { user } = this.props;
    const className = this.props.className || '';
    return (
      <img
        src={ user.photo.url }
        ref="photo"
        className={ className }
        alt={ user.name }
        onError={ this.onPhotoError }
      />
    );
  }
}
