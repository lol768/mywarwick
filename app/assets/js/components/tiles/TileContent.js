/* eslint react/prop-types: 0, react/sort-comp: 0 */
import React, { Component } from 'react';

export default class TileContent extends Component {

  // when do we consider the tile to have no valid content
  isEmpty(content) {
    return !content.items || content.items.length === 0;
  }

  contentOrDefault(content, fetchedAt, contentFunction) {
    const defaultText = (content.defaultText === undefined) ?
      'Nothing to show.' : content.defaultText;
    if (this.isEmpty(content)) {
      return <span>{defaultText}</span>;
    }
    return contentFunction.call(this, content, fetchedAt);
  }

  getBodyInternal(content, fetchedAt) {
    return this.contentOrDefault(content, fetchedAt, this.getBody);
  }

  // when overriding arguments are - content, fetchedAt
  getBody() {
    throw new TypeError('Must implement getBody');
  }

  static canZoom() {
    return false;
  }

  getZoomedBodyInternal(content, fetchedAt) {
    return this.contentOrDefault(content, fetchedAt, this.getZoomedBody);
  }

  getZoomedBody(content, fetchedAt) {
    return this.getBody(content, fetchedAt);
  }

  render() {
    const { content, zoomed, fetchedAt } = this.props;
    if (content) {
      return zoomed ?
        this.getZoomedBodyInternal(content, fetchedAt) : this.getBodyInternal(content, fetchedAt);
    }
    return null;
  }

}
