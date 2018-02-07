import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { Checkbox } from '../../components/ui/Checkbox';
import { connect } from 'react-redux';

export class NewsCategoryPicker extends React.PureComponent {
  static propTypes = {
    newsCategories: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string,
      name: PropTypes.string,
    })),
    formData: PropTypes.objectOf(PropTypes.shape({
      chosenCategories: PropTypes.arrayOf(PropTypes.string),
      ignoreCategories: PropTypes.bool,
    })),
    audienceDidUpdate: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.state = {
      ignoreCategories: Boolean(this.props.formData.ignoreCategories),
      chosenCategories: this.props.formData.chosenCategories.slice(),
    };
    this.handleCategoriesChange = this.handleCategoriesChange.bind(this);
    this.handleIgnore = this.handleIgnore.bind(this);
    this.updateAudienceIndicator = this.updateAudienceIndicator.bind(this);
  }

  updateAudienceIndicator() {
    this.props.audienceDidUpdate();
  }

  handleCategoriesChange(value) {
    this.setState({
      chosenCategories: _.includes(this.state.chosenCategories, value) ?
        _.remove(this.state.chosenCategories.slice(), id => id !== value) :
        this.state.chosenCategories.slice().concat([value])
    });
    this.updateAudienceIndicator();
  }

  handleIgnore() {
    this.setState({
      ignoreCategories: !Boolean(this.state.ignoreCategories),
    });
    this.updateAudienceIndicator();
  }

  makeOptions() {
    return (<div>
      {
        _.map(this.props.newsCategories, (name, id) =>
          (<Checkbox
            key={id}
            handleChange={this.handleCategoriesChange}
            label={name}
            name="categories[]"
            formPath=""
            value={id}
            isChecked={_.includes(this.state.chosenCategories, id)}
          />),
        )
      }
    </div>);
  }

  render() {
    return (
      <div>
        <label className="control-label">
          What categories should this news to be tagged with?
        </label>
        {this.makeOptions()}
        <Checkbox
          handleChange={this.handleIgnore}
          name="item.ignoreCategories"
          label={'Show to everyone in the audience, regardless of their category preferences'}
          isChecked={this.state.ignoreCategories}
          value={this.state.ignoreCategories}
          formPath=""
        />
      </div>
    );
  }
}

function mapDispatchToProps(dispatch) {
  return ({
    audienceDidUpdate: components => dispatch({
      type: 'AUDIENCE_UPDATE',
      components,
    }),
  });
}

export default connect(_, mapDispatchToProps)(NewsCategoryPicker);
