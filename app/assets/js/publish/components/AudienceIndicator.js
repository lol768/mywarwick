import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import log from 'loglevel';
import $ from 'jquery';
import _ from 'lodash-es';
import Hyperlink from '../../components/ui/Hyperlink';

class AudienceIndicator extends React.PureComponent {
  static propTypes = {
    fetching: PropTypes.bool,
    error: PropTypes.bool,
    empty: PropTypes.bool,
    public: PropTypes.bool,
    baseAudience: PropTypes.number,
    categorySubset: PropTypes.number,
    audienceComponents: PropTypes.object,
    store: PropTypes.object.isRequired,
    promiseSubmit: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.state = {
      baseAudience: 0,
      fetching: false,
      public: false,
    };
    this.fetchAudienceEstimate = this.fetchAudienceEstimate.bind(this);
    this.onAudienceChange = this.onAudienceChange.bind(this);
    this.readableAudienceComponents = this.readableAudienceComponents.bind(this);
  }

  componentDidMount() {
    $('[data-toggle="tooltip"]').tooltip();
    this.fetchAudienceEstimate();
  }

  componentWillReceiveProps() {
    this.onAudienceChange();
  }

  onAudienceChange() {
    _.defer(_.debounce(this.fetchAudienceEstimate, 500));
  }

  fetchAudienceEstimate() {
    this.setState({ fetching: true });
    const $form = $($('.split-form').get(0));

    this.props.promiseSubmit($form, {
      url: $form.attr('data-audience-action'),
      dataType: 'json',
    })
      .then(({ data: { baseAudience } }) =>
        setTimeout(() => this.setState({ baseAudience, fetching: false }), 200),
      )
      .catch((e) => {
        this.setState({ baseAudience: 0 });
        log.error('Audience estimate returned error', e);
      })
      .then(() => setTimeout(() => this.setState({ fetching: false }), 200),
      );
  }

  readableAudienceComponents() {
    const { audienceComponents } = this.props;
    const dept = audienceComponents.department;

    if (audienceComponents.audience) {
      const isUniWide = audienceComponents.audience.universityWide !== undefined;
      const audience = this.props.audienceComponents.audience[isUniWide ? 'universityWide' : 'department'];

      if (audience !== undefined) {
        if ('Dept:All' in audience && dept.name !== undefined) {
          return <div>{`Everyone in ${dept.name}`}</div>;
        }

        return (<div> {
          _.map(audience.groups, (v, k) => {
            switch (k) {
              case 'modules':
                return _.map(v, module => (<div key={k}>{module.text || module.value}</div>));
              case 'seminarGroups':
                return _.map(v, seminar => (<div key={k}>{seminar.text}</div>));
              case 'listOfUsercodes':
                if (v !== undefined) { return <div key={k}>{`${v.length} usercodes`}</div>; }
                return null; // lint made me do it
              case 'staffRelationships':
                return _.flatMap(v, rel => rel.options.map(opt => _.map(opt, val => (
                  <div
                    key={k}
                  >{val.selected ? `${_.startCase(val.studentRole)}s of ${rel.text}` : ''}</div>
                ))));
              default: {
                const group = _.startCase(_.replace(k, 'Dept:', ''));
                return (<div key={k}>{`All ${group} in ${_.startsWith(k, 'Dept:') ? dept.name : 'the University'}`}</div>);
              }
            }
          })
        } </div>);
      }
    }
    return null;
  }

  render() {
    const { baseAudience, fetching } = this.state;

    if (this.state.public) {
      return (
        <div className="alert alert-info">
          <div>Public audience</div>
        </div>
      );
    }

    const baseNum = baseAudience !== undefined ? baseAudience.toLocaleString() : '0';

    return (
      <div className="alert alert-info">
        <div>
          <p>When sending alerts, please remember that alerts should be specific or personal to the recipient, and something they need to be aware of or take action on immediately, and concise - a sentence or two at most. <Hyperlink href="https://warwick.ac.uk/mw-support/faqs/usingalerts">More info...</Hyperlink></p>
        </div>

        <div className="pull-right">
          <i
            className="fa fa-info-circle"
            data-toggle="tooltip"
            data-placement="left"
            title="Estimated audience size will be shown here, when audience and categories
        have been selected"
          />
        </div>
        <div>This alert will be published to:</div>
        <div className="audience-component-list">{this.readableAudienceComponents()}</div>
        <div>{fetching ? <i className="fa fa-spin fa-refresh" /> : `(${baseNum} people)`}</div>
      </div>
    );
  }
}

function select(store) {
  return {
    audienceComponents: store.audience,
  };
}

export default connect(select)(AudienceIndicator);
