/* eslint-env browser */
import React from 'react';
import * as PropTypes from 'prop-types';
import $ from 'jquery';

export default class BootstrapModal extends React.PureComponent {
  static propTypes = {
    id: PropTypes.string,
    children: PropTypes.node,
  }

  componentDidMount() {
    $(document.body).addClass('modal-open');
  }

  componentWillUnmount() {
    $(document.body).removeClass('modal-open');
  }

  render() {
    return (
      <div>
        <div className="modal-backdrop in" />
        <div className="modal" id={this.props.id} tabIndex="-1" role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              {this.props.children}
            </div>
          </div>
        </div>
      </div>
    );
  }
}
