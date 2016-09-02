import React, { PropTypes } from 'react';
import Multiselect from 'react-bootstrap-multiselect';
import * as newsCategories from '../../state/news-categories';
import _ from 'lodash';

export default class NewsCategoriesView extends React.Component {

  constructor(props) {
    super(props);

    this.onChange = this.onChange.bind(this);
    this.buttonText = this.buttonText.bind(this);
  }

  onChange(options) {
    const { subscribed, dispatch } = this.props;

    _(options).each(option => {
      const id = option.value;

      if (subscribed.has(id)) {
        dispatch(newsCategories.unsubscribe(id));
      } else {
        dispatch(newsCategories.subscribe(id));
      }
    });
  }

  buttonText({ length: optionsLen }) {
    const { items: { length: totalLen } } = this.props;
    return `Categories (${optionsLen === totalLen ? 'all' : `${optionsLen}/${totalLen}`})`;
  }

  render() {
    const { items, subscribed } = this.props;

    const data = items.map(item => ({
      value: item.id,
      label: item.name,
      selected: subscribed.has(item.id),
    }));

    return (
      <div className="container-fluid news-categories">
        <Multiselect multiple
          data={ data }
          onChange={ this.onChange }
          buttonText={ this.buttonText }
        />
      </div>
    );
  }

}

NewsCategoriesView.propTypes = {
  dispatch: PropTypes.func.isRequired,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  subscribed: PropTypes.instanceOf(Set).isRequired,
};

