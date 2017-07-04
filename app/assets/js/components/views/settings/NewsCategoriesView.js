import React from 'react';
import * as newsCategories from '../../../state/news-categories';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import CheckboxListGroupItem from '../../ui/CheckboxListGroupItem';

class NewsCategoriesView extends HideableView {

  static propTypes = {
    isOnline: PropTypes.bool.isRequired,
    dispatch: PropTypes.func.isRequired,
    fetching: PropTypes.bool.isRequired,
    failed: PropTypes.bool.isRequired,
    subscribed: PropTypes.arrayOf(PropTypes.string).isRequired,
    categories: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    })).isRequired,
  };

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
    this.subscribe = this.subscribe.bind(this);
    this.unsubscribe = this.unsubscribe.bind(this);

    this.state = this.buildState(props);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(this.buildState(nextProps));
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(newsCategories.fetch());
  }

  onClick(id) {
    if (!this.props.isOnline) return;
    const checked = !this.state[id];
    this.setState({ [id]: checked });
    if (checked) {
      this.subscribe(id);
    } else {
      this.unsubscribe(id);
    }
  }

  subscribe(id) {
    this.props.dispatch(newsCategories.subscribe(id));
  }

  unsubscribe(id) {
    this.props.dispatch(newsCategories.unsubscribe(id));
  }

  buildState(props) {
    return _.mapValues(
      _.keyBy(props.categories, v => v.id),
      (v, key) => _.includes(props.subscribed, key)
    );
  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-1">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>News categories</h3>
            </div>
          </div>
        </div>

        <div className="list-group setting-colour-1">
          { _.map(this.props.categories, category =>
            <CheckboxListGroupItem key={ category.id } id={ `category-${category.id}` }
              icon="newspaper-o" description={ category.name } value={ category.id }
              onClick={ this.onClick } checked={ this.state[category.id] }
              disabled={ !this.props.isOnline }
            />
          ) }
        </div>
      </div>
    );
  }

}

function select(state) {
  return {
    fetching: state.newsCategories.fetching,
    failed: state.newsCategories.failed,
    subscribed: state.newsCategories.subscribed,
    categories: state.newsCategories.items,
    isOnline: state.device.isOnline,
  };
}

export default connect(select)(NewsCategoriesView);
