import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import log from 'loglevel';
import $ from 'jquery';
import _ from 'lodash-es';
import Hyperlink from '../../components/ui/Hyperlink';

export class AudienceIndicator extends React.PureComponent {
  static propTypes = {
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
    this.onAudienceChange = _.debounce(this.onAudienceChange.bind(this), 500);
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
    this.fetchAudienceEstimate();
  }

  fetchAudienceEstimate() {
    this.setState({ fetching: true });
    const $form = $($('.split-form').get(0));

    this.props.promiseSubmit($form, {
      url: $form.attr('data-audience-action'),
      dataType: 'json',
    })
      .then(({ data: { baseAudience } }) =>
        this.setState({ baseAudience, fetching: false }),
      )
      .catch((e) => {
        this.setState({ baseAudience: 0 });
        log.error('Audience estimate returned error', e);
      })
      .then(() => this.setState({ fetching: false }),
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
          _.map(audience.groups, (components, audienceType) => {
            switch (audienceType) {
              case 'modules':
                return _.map(components, module =>
                  (<div key={audienceType}>{module.text || module.value}</div>));
              case 'seminarGroups':
                return _.map(components, seminar => (<div key={audienceType}>{seminar.text}</div>));
              case 'listOfUsercodes':
                if (components !== undefined) {
                  return <div key={audienceType}>{`${components.length} usercodes`}</div>;
                }
                return null; // lint made me do it
              case 'staffRelationships':
                return _.flatMap(components, rel => rel.options.map(opt => _.map(opt, val => (
                  <div
                    key={audienceType}
                  >{val.selected ? `${_.startCase(val.studentRole)}s of ${rel.text}` : ''}</div>
                ))));
              default: {
                const group = _.startCase(_.replace(audienceType, 'Dept:', ''));
                return (isUniWide || !_.isEmpty(dept.name)) ?
                  (<div key={audienceType}>{`All ${group} in ${_.startsWith(audienceType, 'Dept:') ? dept.name : 'the University'}`}</div>)
                  : null;
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
          <p>When sending alerts, please remember that alerts should be specific or personal to the
            recipient, and something they need to be aware of or take action on immediately, and
            concise - a sentence or two at most. <Hyperlink
              href="https://warwick.ac.uk/mw-support/faqs/usingalerts"
            >More info...</Hyperlink></p>
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
