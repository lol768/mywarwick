import React from 'react';
import * as PropTypes from 'prop-types';
import HyperLink from './Hyperlink';
import BootstrapModal from './BootstrapModal';
import _ from 'lodash-es';

export default class DismissableInfoModal extends React.PureComponent {
  static propTypes = {
    heading: PropTypes.string,
    subHeadings: PropTypes.arrayOf(PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.node,
    ])),
    children: PropTypes.node,
    href: PropTypes.string,
    onDismiss: PropTypes.func,
    moreButton: PropTypes.element,
  };

  renderMoreButton() {
    if (this.props.moreButton) {
      return this.props.moreButton;
    } else if (this.props.href) {
      return (
        <HyperLink
          type="button"
          className="btn btn-default"
          href={this.props.href}
        >
          More
        </HyperLink>
      );
    }
    return null;
  }

  render() {
    return (
      <BootstrapModal id={this.props.heading}>
        <div className="modal-header">
          <h5 className="modal-title">
            {this.props.heading}
            {_.map(this.props.subHeadings, (subHeading, i) =>
              <small key={i}>{subHeading}</small>,
            )}
          </h5>
        </div>
        <div className="modal-body">
          {this.props.children}
        </div>
        <div className="modal-footer">
          {this.renderMoreButton()}
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
      </BootstrapModal>
    );
  }
}
