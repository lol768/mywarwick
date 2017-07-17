/* eslint no-unused-vars:0 */
import React from 'react';
import * as PropTypes from 'prop-types';

export default class HideableView extends React.PureComponent {

  static contextTypes = {
    visibility: PropTypes.object.isRequired,
  };

  componentDidMount() {
    this.context.visibility.subscribe(event => {
      switch (event) {
        case 'willShow': this.componentWillShow(); break;
        case 'didShow': this.componentDidShow(); break;
        case 'willHide': this.componentWillHide(); break;
        case 'didHide': this.componentDidHide(); break;
        default: throw new Error(`Unexpected value ${event}`);
      }
    });
  }

  componentWillShow() {}

  componentDidShow() {}

  componentWillHide() {}

  componentDidHide() {}

}
