import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import formatDate from '../../dateFormatter';

export default class NewsItem extends ReactComponent {

  render() {
    const { url, title, children, publicationDate, source } = this.props;

    return (
      <article className="news-item">
        <div className="news-item__body">
          <h1 className="news-item__title">
            <a href={url} target="_blank">
              {title}
            </a>
          </h1>

          <div className="news-item__content">
            {children}
          </div>

          <div className="news-item__footer">
            <p>
              {formatDate(publicationDate, new Date(), true)}
            </p>
            <p>
              <i className="fa fa-fw fa-circle" style={{ color: source.colour }}> </i>
              {source.title}
            </p>
          </div>
        </div>
      </article>
    );
  }

}
