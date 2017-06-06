import React from 'react';
import UtilityBar from './UtilityBar';

import $ from 'jquery';
import ReactDOM from 'react-dom';

export default class MastheadMobile extends React.Component {

  componentDidMount() {
    $(ReactDOM.findDOMNode(this)).on('touchmove', (e) => e.preventDefault());
  }

  render() {
    return (
      <div className="start-masthead use-popover">
        { this.props.showEditButton &&
        <div className="edit-btn pulse" onClick={this.props.onEdit}>
          { this.props.editing ?
            <i className="fa fa-fw fa-check"> </i> :
            <i className="fa fa-fw fa-cog"> </i>
          }
        </div>
        }
        <div className="back-btn" onClick={this.props.onBackClick}>
          <i className="fa fa-chevron-left" />
          Back
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
  path: React.PropTypes.string,
  onBackClick: React.PropTypes.func,
  onEdit: React.PropTypes.func,
  editing: React.PropTypes.bool,
  showEditButton: React.PropTypes.bool,
};
