import React, { PropTypes } from 'react';
import * as newsCategories from '../../state/news-categories';
import ColumnLayout from '../ui/ColumnLayout';

export default class NewsCategoriesView extends React.Component {

  onChange(id) {
    return () => {
      const { subscribed, dispatch } = this.props;

      if (subscribed.has(id)) {
        dispatch(newsCategories.unsubscribe(id));
      } else {
        dispatch(newsCategories.subscribe(id));
      }
    };
  }

  render() {
    const { items, subscribed } = this.props;

    const checkboxes = items.map(({ id, name }) =>
      <Checkbox key={ id } label={ name }
        checked={ subscribed.has(id) }
        onChange={ this.onChange(id) }
      />
    );

    return (
      <div className="container-fluid news-categories">
        <ColumnLayout columns={ 2 }>
          { checkboxes }
        </ColumnLayout>
      </div>
    );
  }

}

NewsCategoriesView.propTypes = {
  dispatch: PropTypes.func.isRequired,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  subscribed: PropTypes.instanceOf(Set).isRequired,
};

const Checkbox = ({ label, checked, onChange }) => (
  <div className="checkbox">
    <label>
      <input type="checkbox" checked={ checked } onChange={ onChange } />
      { label }
    </label>
  </div>
);

Checkbox.propTypes = {
  checked: PropTypes.bool.isRequired,
  label: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
};

