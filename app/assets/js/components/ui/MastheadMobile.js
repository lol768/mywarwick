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
        <div style={{ float: 'left', width: 44, textAlign: 'center' }}>
          { this.props.editing ?
            <i className="fa fa-fw fa-lg fa-check" onClick={this.props.onEdit}> </i> :
            <i className="fa fa-fw fa-lg fa-pencil" onClick={this.props.onEdit}> </i>
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
  zoomedTile: React.PropTypes.string,
  path: React.PropTypes.string,
  onBackClick: React.PropTypes.func,
  onEdit: React.PropTypes.func,
  editing: React.PropTypes.bool,
  showEditButton: React.PropTypes.bool,
};
