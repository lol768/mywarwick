import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import formatDate from '../../dateFormatter';
import Hyperlink from './Hyperlink';

const html = (content) => ({ __html: content.replace(/<br[ /]+?>/g, '') });

export default class NewsItem extends ReactComponent {

  render() {
    const { link, title, publishDate, text } = this.props;
    const url = link && link.href;
    const moreLink = link ? (<Hyperlink href={link.href}>{link.text}</Hyperlink>) : null;

    return (
      <article className="news-item">
        <div className="news-item__body">
          <h1 className="news-item__title">
            <Hyperlink href={url}>
              {title}
            </Hyperlink>
          </h1>

          <div className="news-item__content">
            <div dangerouslySetInnerHTML={html(text)}></div>
          </div>

          <div className="news-item__footer">
            <p>
              {formatDate(publishDate, new Date(), true)}
            </p>
            { moreLink }
          </div>
        </div>
      </article>
    );
  }

}
