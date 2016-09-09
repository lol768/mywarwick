import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import { connect } from 'react-redux';

import * as dateFormats from '../../dateFormats';
import Hyperlink from './Hyperlink';

// Convert newlines to paragraphs.
export const render = (content) =>
  content
    .split('\n')
    .map(t => t.trim())
    .filter(t => t.length)
    .map((t, i) => <p key={i}>{t}</p>);

class NewsItem extends ReactComponent {

  render() {
    const { id, link, title, publishDate, text, imageId, categories, width } = this.props;
    const { analyticsClientId } = this.props;

    const url = link && `/news/${id}/redirect?clientId=${analyticsClientId}`;
    const moreLink = link ? (<p><Hyperlink href={url}>{link.text}</Hyperlink></p>) : null;

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
                src={ `/api/news/images/${imageId}?width=${width * window.devicePixelRatio}` }
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
              {dateFormats.forNewsItem(publishDate)}
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

const select = (state) => ({
  analyticsClientId: state.analytics.clientId,
});
export default connect(select)(NewsItem);
