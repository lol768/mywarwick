import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import { connect } from 'react-redux';

import formatDate from '../../dateFormatter';
import Hyperlink from './Hyperlink';

// Convert newlines to paragraphs.
export const render = (content) =>
  content
    .split('\n')
    .map(t => t.trim())
    .filter(t => t.length)
    .map((t, i) => <p key={i}>{t}</p>);

export default class NewsItem extends ReactComponent {

  render() {
    const { link, title, publishDate, text, imageId, width, categories } = this.props;
    const url = link && link.href;
    const moreLink = link ? (<p><Hyperlink href={link.href}>{link.text}</Hyperlink></p>) : null;

    return (
      <article className="news-item">
        <div className="news-item__body">
          <h1 className="news-item__title">
            <Hyperlink href={url}>
              {title}
            </Hyperlink>
          </h1>

          { imageId ?
            <div className="news-item__image">
              <img
                src={ `/api/news/images/${imageId}?width=${width}` }
                alt={ title }
              />
            </div>
            : null }

          <div className="news-item__content">
            {render(text)}
          </div>

          <div className="news-item__footer">
            { moreLink }
            <div className="news-item__category-tags">
              {categories.map(c => <NewsItemTag key={c.id} name={c.name} />)}
            </div>
            <p>
              {formatDate(publishDate, new Date(), true)}
            </p>
          </div>
        </div>
      </article>
    );
  }
}

const NewsItemTag = props =>
  <span className="badge">
    { props.name }
  </span>;

NewsItemTag.propTypes = {
  name: React.PropTypes.string.isRequired,
};

const select = (state) => state.device;
export default connect(select)(NewsItem);
