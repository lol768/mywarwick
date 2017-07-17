import React from 'react';
import * as PropTypes from 'prop-types';
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

class NewsItem extends React.PureComponent {

  static propTypes = {
    id: PropTypes.string.isRequired,
    link: PropTypes.shape({
      text: PropTypes.string.isRequired,
    }),
    title: PropTypes.string.isRequired,
    publishDate: PropTypes.string.isRequired,
    text: PropTypes.string.isRequired,
    imageId: PropTypes.string,
    categories: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    })),
    width: PropTypes.number.isRequired,
    analyticsClientId: PropTypes.string.isRequired,
  };

  render() {
    const { id, link, title, publishDate, text, imageId, categories, width } = this.props;
    const { analyticsClientId } = this.props;

    const url = link && `/news/${id}/redirect?clientId=${analyticsClientId}`;
    const moreLink = link ? (<p><Hyperlink href={url}>{link.text}</Hyperlink></p>) : null;

    const imageWidth = width * (window.devicePixelRatio || 1);

    return (
      <article className="news-item">
        <div className="news-item__body">
          <h1 className="news-item__title">
            <Hyperlink href={url}>
              {title}
            </Hyperlink>
          </h1>

          { imageId && <NewsItemImage id={imageId} width={imageWidth} alt={title} /> }

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

export function NewsItemImage({ id, width, alt }) {
  return (
    <div className="news-item__image">
      <img src={`/api/news/images/${id}?width=${Math.round(width)}`} alt={alt} />
    </div>
  );
}

NewsItemImage.propTypes = {
  id: PropTypes.string.isRequired,
  width: PropTypes.number.isRequired,
  alt: PropTypes.string.isRequired,
};

const NewsItemTag = props =>
  <span className="badge">
    { props.name }
  </span>;

NewsItemTag.propTypes = {
  name: PropTypes.string.isRequired,
};

const select = (state) => ({
  analyticsClientId: state.analytics.clientId,
});
export default connect(select)(NewsItem);
