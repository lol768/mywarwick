/* eslint no-unused-vars:0 */
import React, { PropTypes } from 'react';

export default class HideableView extends React.PureComponent {

  static contextTypes = {
    visibility: PropTypes.object.isRequired,
  };

  componentDidMount() {
    this.context.visibility.subscribe(event => {
      switch (event) {
        case 'willShow': return this.componentWillShow();
        case 'didShow': return this.componentDidShow();
        case 'willHide': return this.componentWillHide();
        case 'didHide': return this.componentDidHide();
      }
    });
  }

  componentWillShow() {}

  componentDidShow() {}

  componentWillHide() {}

  componentDidHide() {}

}
