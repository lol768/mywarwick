import React, { Component } from 'react';

var RequireUser = (ComposedComponent) => class extends Component {
  isDisabled() {
    console.log("auth"+ this.props.authenticated);
    return (this.props.requireUser || false) && !this.props.authenticated;
  }

  render() {
    return <ComposedComponent isDisabled={this.isDisabled()} {...this.props} {...this.state} />;
  }
};

export default RequireUser;