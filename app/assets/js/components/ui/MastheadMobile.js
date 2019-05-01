import React from 'react';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import * as PropTypes from 'prop-types';
import $ from 'jquery';
import ReactDOM from 'react-dom';
import UtilityBar from './UtilityBar';
import { Routes } from '../AppRoot';

export default class MastheadMobile extends React.PureComponent {
  static buildState(props) {
    return {
      backButtonVisible:
      props.path.indexOf(`/${Routes.SETTINGS}`) === 0 ||
      props.path.indexOf(`/${Routes.TILES}`) === 0 ||
      (props.features.updateTileEditUI && props.path.indexOf(`/${Routes.EDIT}`) === 0) ||
      props.path.indexOf(`/${Routes.EDIT}/${Routes.ADD}`) === 0,
      backButtonText: props.path === `/${Routes.SETTINGS}` ? 'Done' : 'Back',
    };
  }

  constructor(props) {
    super(props);

    this.state = MastheadMobile.buildState(props);
  }

  componentDidMount() {
    $(ReactDOM.findDOMNode(this)).on('touchmove', e => e.preventDefault());
  }

  componentWillReceiveProps(newProps) {
    this.setState(MastheadMobile.buildState(newProps));
  }

  render() {
    return (
      <div className="start-masthead use-popover">
        { this.props.showSettingsButton &&
        <div
          className="settings-btn"
          onClick={this.props.onSettings}
          onKeyUp={this.props.onSettings}
          role="button"
          tabIndex={0}
        >
          <i className="fal fa-fw fa-cog" />
        </div>
        }
        { this.props.showEditButton &&
        <div
          className="edit-btn pulse"
          onClick={this.props.onEditComplete}
          onKeyUp={this.props.onEditComplete}
          role="button"
          tabIndex={0}
        >
          {this.props.editing ?
            <i className="fal fa-fw fa-check" /> :
            <i className="fal fa-fw fa-arrows-alt" />
          }
        </div>
        }
        <ReactCSSTransitionGroup
          transitionName="btn-slide"
          transitionAppear
          transitionAppearTimeout={300}
          transitionEnterTimeout={300}
          transitionLeaveTimeout={300}
        >
          { this.state.backButtonVisible &&
          <div
            className="back-btn"
            onClick={this.props.onBackClick}
            onKeyUp={this.props.onBackClick}
            role="button"
            tabIndex={0}
          >
            <i className="fal fa-chevron-left" />
            { this.state.backButtonText }
          </div>
          }
        </ReactCSSTransitionGroup>

        <div className="masthead-title">
          <span className="light">My</span> Warwick
        </div>
        <nav className="id7-utility-bar">
          <UtilityBar {...this.props} />
        </nav>
      </div>
    );
  }
}

MastheadMobile.propTypes = {
  path: PropTypes.string,
  onBackClick: PropTypes.func,
  onEditComplete: PropTypes.func,
  editing: PropTypes.bool,
  showEditButton: PropTypes.bool,
  onSettings: PropTypes.func,
  showSettingsButton: PropTypes.bool,
  features: PropTypes.object.isRequired,
};
