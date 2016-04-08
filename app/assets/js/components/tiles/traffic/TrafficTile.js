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

  conditionList() {
    const itsHomeTime = new Date().getHours() >= HOME_TIME;
    const trafficConditions = this.props.content.items.filter(r => r.route.inbound !== itsHomeTime)
      .map(condition => <TrafficCondition key={condition.route.name} {...condition} />);

    if (trafficConditions.length > 0) {
      return (
        <ul className="list-unstyled traffic--conditions">
          {trafficConditions}
        </ul>
      );
    }
  }

  renderIfFresh(contentFunction) {
    const msSinceFetch = new Date().getTime() - this.props.fetchedAt;
    if (msSinceFetch > STALE_AFTER) {
      return <div>Unable to show recent traffic information.</div>;
    }

    return contentFunction.call(this);
  }

  static canZoom() {
    return true;
  }

  _getBody() {
    const { content, content: { alerts: { items } } } = this.props;

    return (
      <div className="tile--traffic">
        {this.conditionList()}
        {
          items.length > 0 ?
            <div className="tile__item">
              <i className={classNames('fa', 'fa-exclamation-triangle')}> </i>
              <a href={items.length === 1 ? items[0].url.href : content.alerts.href}>
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

  getBody() {
    return this.renderIfFresh(this._getBody);
  }

  _getZoomedBody() {
    const { content } = this.props;

    return (
      <div className="tile--traffic">
        <div className={classNames('col-xs-12', 'col-sm-4')}>
          {this.conditionList()}
        </div>
        <div className={classNames('col-xs-12', 'col-sm-8')}>
          { content.alerts.items.slice(0, 2).map(a =>
            <TrafficAlert key={a.title} title={a.title} href={a.url.href}/>
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

  getZoomedBody() {
    return this.renderIfFresh(this._getZoomedBody);
  }
}
