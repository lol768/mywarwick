import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';

const Skycons = require('skycons')(window);

export default class Skycon extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      skycons: new Skycons({ color: this.props.color, resizeClear: true }),
    };
  }

  componentDidMount() {
    this.state.skycons.add(ReactDOM.findDOMNode(this), Skycons[this.props.icon]);
    if (this.props.autoplay) {
      this.state.skycons.play();
    }
  }

  componentWillReceiveProps(nextProps) {
    this.state.skycons.set(ReactDOM.findDOMNode(this), Skycons[nextProps.icon]);
  }

  componentWillUnmount() {
    this.state.skycons.pause();
    this.state.skycons.remove(ReactDOM.findDOMNode(this));
  }

  play() {
    this.state.skycons.play();
  }

  pause() {
    this.state.skycons.pause();
  }

  render() {
    const props = {};
    for (const prop in this.props) {
      if (this.props.hasOwnProperty(prop)) {
        props[prop] = this.props[prop];
      }
    }
    delete props.autoplay;
    return (
      <canvas {...props} />
    );
  }
}

Skycon.displayName = 'ReactSkycons';

Skycon.propTypes = {
  color: PropTypes.string,
  autoplay: PropTypes.bool,
  icon: PropTypes.oneOf([
    'CLEAR_DAY',
    'CLEAR_NIGHT',
    'PARTLY_CLOUDY_DAY',
    'PARTLY_CLOUDY_NIGHT',
    'CLOUDY',
    'RAIN',
    'SLEET',
    'SNOW',
    'WIND',
    'FOG',
  ]),
};

Skycon.defaultProps = {
  color: 'white',
  autoplay: true,
};
