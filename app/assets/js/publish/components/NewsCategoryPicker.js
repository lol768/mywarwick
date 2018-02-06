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
    audienceDidUpdate: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.state = {
      ignoreCategories: false,
    };
    this.handleCategoriesChange = this.handleCategoriesChange.bind(this);
    this.handleIgnore = this.handleIgnore.bind(this);
    this.updateAudienceIndicator = this.updateAudienceIndicator.bind(this);
  }

  updateAudienceIndicator() {
    this.props.audienceDidUpdate();
  }

  handleCategoriesChange() {
    this.updateAudienceIndicator();
  }

  handleIgnore(value) {
    this.setState({
      ignoreCategories: !value,
    });
    this.updateAudienceIndicator();
  }

  makeOptions(newsCategories) {
    return (<div>
      {
        _.map(newsCategories, (name, id) =>
          (<Checkbox
            key={id}
            handleChange={this.handleCategoriesChange}
            label={name}
            name="categories[]"
            formPath=""
            value={id}
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
        {this.makeOptions(this.props.newsCategories)}
        <Checkbox
          handleChange={this.handleIgnore}
          name="item.ignoreCategories"
          label={'Show to everyone in the audience, regardless of their category preferences'}
          checked={this.state.ignoreCategories}
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
