import React from 'react';
import * as PropTypes from 'prop-types';

export default class DismissableInfoModal extends React.PureComponent {
  static propTypes = {
    heading: PropTypes.string,
    subHeading: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.node,
    ]),
    children: PropTypes.node,
    onDismiss: PropTypes.func,
  };

  render() {
    return (
      <div>
        <div className="modal-backdrop in" />
        <div className="modal" id={this.props.heading} tabIndex="-1" role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  {this.props.heading}
                  <small>
                    &nbsp;{this.props.subHeading}
                  </small>
                </h5>
              </div>
              <div className="modal-body">
                {this.props.children}
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-default"
                  data-dismiss="modal"
                  onClick={this.props.onDismiss}
                  onKeyUp={this.props.onDismiss}
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
