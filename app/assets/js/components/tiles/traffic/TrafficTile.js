import React from 'react';
import TileContent from '../TileContent';
import classNames from 'classnames';

import TrafficAlert from './TrafficAlert';
import TrafficCondition from './TrafficCondition';

// at what hour of the day we assume most users will be heading away from campus
const HOME_TIME = 15;
// we consider the information on the tile as stale after 20 minutes
const STALE_AFTER = 1200000;

export default class TrafficTile extends TileContent {

  conditionList(content) {
    const itsHomeTime = new Date().getHours() >= HOME_TIME;
    const trafficConditions = content.items.filter(r => r.route.inbound !== itsHomeTime)
      .map(condition => <TrafficCondition key={condition.route.name} {...condition} />);
    if (trafficConditions.length > 0) {
      return (
        <ul className="traffic--conditions">
          {trafficConditions}
        </ul>
      );
    }
    return null;
  }

  checkFresh(content, fetchedAt, renderFunc) {
    const msSinceFetch = new Date().getTime() - fetchedAt;
    if (msSinceFetch > STALE_AFTER) {
      return (<div>Unable to show recent traffic information.</div>);
    }
    return renderFunc.call(this, content);
  }

  static canZoom() {
    return true;
  }

  _getBody(content) {
    return (
      <div className="traffic--tile">
        {this.conditionList(content)}
        {
          content.alerts.items.length > 0 ?
            <div className="tile__item">
              <i className={classNames('fa', 'fa-exclamation-triangle')}> </i>
              <a href="http://www2.warwick.ac.uk/insite/kcm/news/">
                <strong className="alert-count">
                  {`${content.alerts.items.length} traffic alert` +
                   `${content.alerts.items.length > 1 ? 's' : ''}`}
                </strong>
              </a>
            </div>
            : null
        }
      </div>
    );
  }

  getBody(content, fetchedAt) {
    return this.checkFresh(content, fetchedAt, this._getBody);
  }

  _getZoomedBody(content) {
    return (
      <div className="traffic--tile">
        <div className={classNames('col-xs-12', 'col-sm-4')}>
          {this.conditionList(content)}
        </div>
        <div className={classNames('col-xs-12', 'col-sm-8')}>
          { content.alerts.items.slice(0, 2).map(a =>
            <TrafficAlert key={a.title} title={a.title} href={a.url.href} />
          )}
          {
            content.alerts.items.length > 2 ?
              <a className="more-alerts" href={content.alerts.href}>See more ...</a>
              : null
          }
        </div>
      </div>
    );
  }

  getZoomedBody(content, fetchedAt) {
    return this.checkFresh(content, fetchedAt, this._getZoomedBody);
  }
}
