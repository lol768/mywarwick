import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import log from 'loglevel';
import $ from 'jquery';
import _ from 'lodash-es';
import Hyperlink from '../../components/ui/Hyperlink';
import { mkString } from '../../helpers';

export class AudienceIndicator extends React.PureComponent {
  static propTypes = {
    audienceComponents: PropTypes.object,
    promiseSubmit: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.state = {
      baseAudience: 0,
      groupedAudience: {},
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

  componentWillUpdate(nextProps, nextState) {
    $('.split-form').first().data('base-audience', nextState.baseAudience);
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
      .then((result) => {
        const baseAudience = result.data.baseAudience;
        const groupedAudience = result.data.groupedAudience;
        this.setState({
          baseAudience,
          groupedAudience,
          fetching: false,
        });
      })
      .catch((e) => {
        this.setState({
          baseAudience: 0,
          groupedAudience: {},
        });
        log.error('Audience estimate returned error', e);
      })
      .then(() => this.setState({ fetching: false }),
      );
  }

  readableAudienceComponents() {
    const { audienceComponents } = this.props;
    const { fetching, groupedAudience } = this.state;
    const dept = audienceComponents.department;

    const getCount = (group) => {
      const peopleCount = groupedAudience[group] ? groupedAudience[group] : 0;
      return (fetching ?
        <i className="fa fa-spin fa-fw fa-refresh" /> : `${peopleCount} people`);
    };

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
                return _.map(components, ({ text, value }) =>
                  (<div
                    key={`${audienceType}:${value}`}
                  >{text || value}: {getCount(`ModuleAudience(${value})`)}</div>));
              case 'seminarGroups':
                return _.map(components, ({ text, value }) =>
                  (<div
                    key={`${audienceType}:${text}`}
                  >{text}: {getCount(`SeminarGroupAudience(${value})`)}</div>));
              case 'listOfUsercodes':
                return (components !== undefined) ?
                  (<div
                    key={audienceType}
                  >
                    {`Usercodes or university IDs: ${components.length} people`}
                  </div>) :
                  <div />;
              case 'staffRelationships':
                return _.flatMap(components, rel =>
                  rel.options.map(opt =>
                    _.map(opt, val =>
                      (val.selected ?
                        (<div>{`${_.startCase(val.studentRole)}s of ${rel.text}`}: {getCount(`RelationshipAudience(personalTutor,UniversityID(${rel.value}))`)}</div>) :
                        (<div />)
                      ))));
              case 'undergraduates':
                if (components !== undefined) {
                  const subset = dept.name !== undefined ? dept.name : 'the University';
                  if (_.has(components, 'year')) {
                    const years = _.map(components.year, (k, year) => _.last(_.split(year, ':')).toLowerCase());
                    return years.length ?
                      <div key={audienceType}>
                        {`All ${mkString(years)} year Undergraduates in ${subset}`}
                      </div> : null;
                  }
                  return `All Undergraduates in ${subset}`;
                }
                return null;
              default: {
                const group = _.replace(audienceType, 'Dept:', '');
                const groupDisplayName = _.startCase(group);
                return (isUniWide || !_.isEmpty(dept.name)) ?
                  (<div
                    key={audienceType}
                  >{`All ${groupDisplayName} in ${_.startsWith(audienceType, 'Dept:') ? dept.name : 'the University'}`}: {getCount(group)}</div>)
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
            >More info…</Hyperlink></p>
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
        <div>{fetching ?
          <i className="fa fa-spin fa-fw fa-refresh" /> : `(${baseNum} people in total)`}</div>
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
