// react/sort-comp goes crazy in this file - disable it
/* eslint react/prop-types: 0, react/sort-comp: 0 */
import React, { Component } from 'react';

import { localMoment } from '../../dateFormatter.js';
import classNames from 'classnames';

import {EDITING_ANIMATION_DURATION} from '../views/MeView';

import $ from 'jquery';

const SIZE_CLASSES = {
  small: 'col-xs-6 col-sm-6 col-md-3',
  wide: 'col-xs-12 col-sm-12 col-md-6',
  large: 'col-xs-12 col-sm-12 col-md-6'
};

const LONG_PRESS_DURATION_MS = 600;

export default class Tile extends Component {

  contentOrDefault(content, contentFunction) {
    const defaultText = (content.defaultText === undefined) ?
      'Nothing to show.' : content.defaultText;
    if (!content.items || content.items.length === 0) {
      return <span>{defaultText}</span>;
    }
    return contentFunction.call(this, content);
  }

  getBodyInternal(content) {
    return this.contentOrDefault(content, this.getBody);
  }

  /* eslint-disable no-unused-vars */
  getBody(content) {
    throw new TypeError('Must implement getBody');
  }
  /* eslint-enable no-unused-vars */

  canZoom() {
    return false;
  }

  shouldDisplayExpandIcon() {
    return this.props.editing ? false : this.canZoom();
  }

  isZoomed() {
    return this.props.zoomed;
  }

  getZoomedBodyInternal(content) {
    return this.contentOrDefault(content, this.getZoomedBody);
  }

  getZoomedBody(content) {
    return this.getBody(content);
  }

  componentWillEnter(callback) {
    if ('componentWillEnter' in this.props) {
      this.props.componentWillEnter(callback);
    }
  }

  componentWillLeave(callback) {
    if ('componentWillLeave' in this.props) {
      this.props.componentWillLeave(callback);
    }
  }

  getIcon() {
    const { fetching, errors, icon } = this.props;

    if (fetching) {
      return 'fa-refresh fa-spin';
    } else if (errors) {
      return 'fa-exclamation-triangle';
    } else if (icon) {
      return `fa-${icon}`;
    }
    return 'fa-question-circle';
  }

  getIconTitle() {
    const { fetching, errors, title } = this.props;

    if (fetching) {
      return `Refreshing ${title}`;
    } else if (errors) {
      return `An error occurred while refreshing the ${title} tile.
        The error was: ${errors[0].message}`;
    }
    return `Refresh ${title}`;
  }

  onMouseDown() {
    if (!this.props.editing && !this.isZoomed()) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));
      el.stop().transition({
        scale: 0.97
      }, 200);

      this.timeout = setTimeout(this.props.onBeginEditing, LONG_PRESS_DURATION_MS);
    }
  }

  onMouseUp() {
    if (!this.props.editing && !this.isZoomed()) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));
      el.stop().transition({
        scale: 1
      }, 200);

      this.timeout = clearTimeout(this.timeout);
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.editing && !prevProps.editing) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));

      el.stop().transition({
        scale: 1.15
      }, EDITING_ANIMATION_DURATION, 'snap');
    } else if (prevProps.editing && !this.props.editing) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));

      el.stop().transition({
        scale: 1
      }, EDITING_ANIMATION_DURATION, 'snap');
    }
  }

  getResizeControlIcon() {
    return {
      small: 'right',
      wide: 'down',
      large: 'up'
    }[this.getSize()];
  }

  getSize() {
    return this.props.size || this.props.defaultSize;
  }

  render() {
    const props = this.props;

    const icon = (<i
      className={`tile__icon fa fa-fw ${this.getIcon()}`} title={ this.getIconTitle() }
      onClick={this.props.onClickRefresh}
    > </i>);

    const sizeClass = SIZE_CLASSES[this.getSize()];
    const outerClassName = classNames(sizeClass, 'tile__container', {
      'tile--zoomed': props.zoomed,
      'tile--text-btm': true
    });
    const zoomIcon = () => {
      if (this.isZoomed()) {
        return <i className="fa fa-times tile__dismiss" onClick={props.onDismiss}> </i>;
      } else if (this.shouldDisplayExpandIcon()) {
        return <i className="fa fa-expand tile__expand" onClick={props.onExpand}> </i>;
      }
      return null;
    };

    return (
      <div className={outerClassName}>
        <article
          className={classNames('tile', 'tile--' + props.type, 'tile--' + this.getSize(), 'colour-' + props.colour, {'tile--editing': props.editing})}
          onMouseDown={this.onMouseDown.bind(this)} onMouseUp={this.onMouseUp.bind(this)} onMouseOut={this.onMouseUp.bind(this)}
          onTouchStart={this.onMouseDown.bind(this)} onTouchEnd={this.onMouseUp.bind(this)} onTouchCancel={this.onMouseUp.bind(this)}
          ref="tile">
          <div className="tile__edit-control top-left" onClick={this.props.onHide.bind(this)} title="Hide tile">
            <i className="fa fa-fw fa-minus"></i>
          </div>
          <div className="tile__edit-control bottom-right" onClick={this.props.onResize.bind(this)} title={`Make tile ${this.getSize() === 'small' ? 'bigger' : 'smaller'}`}>
            <i className={`fa fa-fw fa-arrow-${this.getResizeControlIcon()}`}></i>
          </div>
          <div className="tile__wrap">
            <header>
              <h1>
                {icon}
                <span className="tile__title">
                  {props.title}
                </span>
              </h1>
              { zoomIcon() }
            </header>
            <div className="tile__body">
              { this.getOuterBody() }
            </div>
          </div>
        </article>
      </div>
    );
  }

  getOuterBody() {
    const { content, errors, zoomed, fetchedAt } = this.props;

    if (content) {
      const body = zoomed ? this.getZoomedBodyInternal(content) : this.getBodyInternal(content);

      if (errors) {
        return (
          <div>
            <div className="tile__last-updated">
              Last updated {localMoment(fetchedAt).calendar()}
            </div>
            {body}
          </div>
        );
      }
      return <div>{body}</div>;
    }
    // Initial load
    return null;
  }

}

/* TODO - include prop validation for Tile that doesn't cause warnings in the extending classes
Tile.propTypes = {
  componentWillEnter: PropTypes.function,
  componentWillLeave: PropTypes.function,
  content: PropTypes.object,
  errors: PropTypes.object,
  fetchedAt: PropTypes.string,
  fetching: PropTypes.boolean,
  icon: PropTypes.string,
  onClickRefresh: PropTypes.function,
  title: PropTypes.string,
  zoomed: PropTypes.boolean,
};
*/
