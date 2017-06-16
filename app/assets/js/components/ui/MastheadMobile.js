import React from 'react';
import * as PropTypes from 'prop-types';
import UtilityBar from './UtilityBar';
import $ from 'jquery';
import ReactDOM from 'react-dom';

export default class MastheadMobile extends React.PureComponent {

  componentDidMount() {
    $(ReactDOM.findDOMNode(this)).on('touchmove', (e) => e.preventDefault());
  }

  render() {
    return (
      <div className="start-masthead use-popover">
        { this.props.showSettingsButton &&
        <div className="settings-btn" onClick={this.props.onSettings}>
          <i className="fa fa-fw fa-cog"> </i>
        </div>
        }
        { this.props.showEditButton &&
        <div className="edit-btn pulse" onClick={this.props.onEdit}>
          { this.props.editing ?
            <i className="fa fa-fw fa-check"> </i> :
            <i className="fa fa-fw fa-arrows"> </i>
          }
        </div>
        }
        <div className="back-btn" onClick={this.props.onBackClick}>
          <i className="fa fa-chevron-left" />
          { this.props.backButtonText }
        </div>
        <div className="masthead-title">
          <span className="light">My</span> Warwick
        </div>
        <nav className="id7-utility-bar">
          <UtilityBar {...this.props} layoutClassName="mobile" />
        </nav>
      </div>
    );
  }

}

MastheadMobile.propTypes = {
  path: PropTypes.string,
  onBackClick: PropTypes.func,
  backButtonText: PropTypes.string,
  onEdit: PropTypes.func,
  editing: PropTypes.bool,
  showEditButton: PropTypes.bool,
  onSettings: PropTypes.func,
  showSettingsButton: PropTypes.bool,
};
