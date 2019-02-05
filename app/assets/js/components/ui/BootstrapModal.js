/* eslint-env browser */
import React from 'react';
import * as PropTypes from 'prop-types';
import $ from 'jquery';
import classNames from 'classnames';

export default class BootstrapModal extends React.PureComponent {
  static propTypes = {
    id: PropTypes.string,
    children: PropTypes.node,
    className: PropTypes.string
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
        <div className={classNames("modal", this.props.className)} id={this.props.id} tabIndex="-1" role="dialog">
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
