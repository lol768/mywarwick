// TODO - include prop validation for Tile that doesn't break the extending tile types
/* eslint react/prop-types:0 */
import React, { Component } from 'react';

import { localMoment } from '../../dateFormatter.js';
import classNames from 'classnames';

const sizeClasses = {
  small: 'col-xs-6 col-md-3',
  large: 'col-xs-12 col-sm-6',
  wide: 'col-xs-12 col-sm-6',
  tall: 'col-xs-12 col-sm-6',
};

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

  render() {
    const props = this.props;

    const icon = (<i
      className={`tile__icon fa fa-fw ${this.getIcon()}`} title={ this.getIconTitle() }
      onClick={this.props.onClickRefresh}
    > </i>);

    const size = props.size || props.defaultSize;
    const sizeClass = sizeClasses[size];
    const outerClassName = classNames(sizeClass, {
      'tile--zoomed': props.zoomed,
      'tile--canZoom': this.canZoom(),
      'tile--text-btm': true,
    });
    const zoomIcon = () => {
      if (this.isZoomed()) {
        return <i className="fa fa-times tile__dismiss" onClick={props.onDismiss}> </i>;
      } else if (this.canZoom()) {
        return <i className="fa fa-expand tile__expand" onClick={props.onExpand}> </i>;
      }
      return null;
    };

    return (
      <div className={outerClassName}>
        <article
          className={
            classNames('tile', `tile--${props.tileType}`, `tile--${size}`, `colour-${props.colour}`)
          }
          ref="tile"
        >
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
