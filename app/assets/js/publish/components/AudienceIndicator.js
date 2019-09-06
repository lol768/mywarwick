import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import log from 'loglevel';
import $ from 'jquery';
import _ from 'lodash-es';
import { titleCase, sentenceCase } from 'change-case';

import { mkString } from '../../helpers';

export class AudienceIndicator extends React.PureComponent {
  static propTypes = {
    audienceComponents: PropTypes.object,
    promiseSubmit: PropTypes.func.isRequired,
    itemType: PropTypes.string.isRequired,
    canEstimateAudience: PropTypes.bool.isRequired,
  };

  static totaliser(error, fetching, baseAudience) {
    if (error) {
      return <div className="media">
        <div className="media-left">
          <i className="fal fa-user-slash"></i>
        </div>
        <div className="media-body">
          Cannot estimate user count
          <div className="small text-muted">
            {error}
          </div>
        </div>
      </div>;
    }

    if (fetching) {
      return <div className="media">
        <div className="media-left">
          <i className="fal fa-spin fa-sync"></i>
        </div>
        <div className="media-body">
          Recalculating user estimate…
        </div>
      </div>;
    }

    if (baseAudience === 0) {
      return <div className="media">
        <div className="media-left">
          <i className="fal fa-user-slash"></i>
        </div>
        <div className="media-body">
          No matching users
        </div>
      </div>;
    }

    if (baseAudience === 1) {
      return <div className="media">
        <div className="media-left">
          <i className="fal fa-user"></i>
        </div>
        <div className="media-body">
          One matching user
        </div>
      </div>;
    }

    return <div className="media">
      <div className="media-left">
        <i className="fal fa-users"></i>
      </div>
      <div className="media-body">
        {baseAudience} matching users
      </div>
    </div>;
  }

  constructor(props) {
    super(props);
    this.state = {
      baseAudience: 0,
      groupedAudience: {},
      fetching: false,
      public: false,
      error: false,
    };
    this.fetchAudienceEstimate = this.fetchAudienceEstimate.bind(this);
    this.onAudienceChange = _.debounce(this.onAudienceChange.bind(this), 500);
    this.parseAudienceComponents = this.parseAudienceComponents.bind(this);
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
    const list = this.parseAudienceComponents();
    if (_.isEmpty(_.compact(list))) {
      this.setState({
        baseAudience: 0,
        groupedAudience: {},
        fetching: false,
        error: false,
      });
    } else {
      this.setState({ fetching: true });
      const $form = $($('.split-form').get(0));

      this.props.promiseSubmit($form, {
        url: $form.attr('data-audience-action'),
        dataType: 'json',
      })
        .then((result) => {
          const { baseAudience, groupedAudience } = result.data;
          this.setState({
            baseAudience,
            groupedAudience,
            fetching: false,
            error: false,
          });
        })
        .catch((e) => {
          const jsonErrors = _.get(e, 'responseJSON.errors', []);
          if (_.some(jsonErrors, 'message', 'error.audience.usercodes.invalid')) {
            this.setState({
              baseAudience: 0,
              groupedAudience: {},
              error: 'One or more usercode or university ID supplied was invalid',
            });
          } else {
            this.setState({
              baseAudience: 0,
              groupedAudience: {},
              error: 'There was an unexpected error estimating the user count',
            });
            log.error('Audience estimate returned error', e);
          }
        })
        .then(() => this.setState({ fetching: false }));
    }
  }

  parseAudienceComponents() {
    const { audienceComponents, canEstimateAudience } = this.props;
    const { fetching, groupedAudience } = this.state;
    const dept = audienceComponents.department;

    const getCount = (groups) => {
      if (canEstimateAudience) {
        const count = _.reduce(groups, (acc, group) => acc + (groupedAudience[group] || 0), 0);
        return (fetching
          ? <i className="fal fa-spin fa-fw fa-sync"/> :
          <span className="badge">{count}</span>);
      }
      return null;
    };

    const renderedAudience = [];

    if (audienceComponents.audience) {
      const isUniWide = audienceComponents.audience.universityWide !== undefined;
      const audience = this.props.audienceComponents.audience[isUniWide ? 'universityWide' : 'department'];

      if (audience !== undefined) {
        if ('Dept:All' in audience && dept.name !== undefined) {
          renderedAudience.push(<li key="all">{`Everyone in ${dept.name}`}</li>);
        }

        _.map(audience.groups, (components, audienceType) => {
          switch (audienceType) {
            case 'hallsOfResidence':
              if (components !== undefined) {
                const halls = components.hall;
                if (halls) {
                  renderedAudience.push(_.map(halls, (value, key) => {
                    const displayName = titleCase(sentenceCase(_.last(key.split(':'))));
                    return (
                      <li key={key}>
                        All residents
                        of {displayName} {getCount([`ResidenceAudience(${displayName.replace(' ', '')})`], halls)}
                      </li>
                    );
                  }));
                }
              }
              break;
            case 'modules':
              renderedAudience.push(_.map(components, ({ text, value }) => (<li
                  key={`${audienceType}:${value}`}
              >{text || value} {getCount([`ModuleAudience(${value})`])}</li>)));
              break;
            case 'seminarGroups':
              renderedAudience.push(_.map(components, ({ text, value }) => (<li
                  key={`${audienceType}:${text}`}
              >{text} {getCount([`SeminarGroupAudience(${value})`])}</li>)));
              break;
            case 'listOfUsercodes':
              if (components !== undefined) {
                const groups = _.filter(
                  _.keys(groupedAudience), key => _.startsWith(key, 'UsercodesAudience')
                );
                renderedAudience.push((<li key={audienceType}>
                  Usercodes or university IDs {getCount(groups)}
                </li>));
              }
              break;
            case 'staffRelationships':
              renderedAudience.push(_.flatMap(
                components,
                rel => rel.options.map(opt => _.map(opt, val => (val.selected
                  ? (<li key={val}>{`${_.startCase(val.studentRole)}s of ${rel.text}`} {getCount([`RelationshipAudience(personalTutor,UniversityID(${rel.value}))`])}</li>)
                  : null
                ))),
              ));
              break;
            case 'undergraduates':
              if (components !== undefined) {
                const subset = dept.name !== undefined ? dept.name : 'the University';
                if (_.has(components, 'year')) {
                  const years = _.map(components.year, (k, year) => _.last(_.split(year, ':')).toLowerCase());
                  if (years.length) {
                    renderedAudience.push(<li key={audienceType}>
                      {`All ${mkString(years)} year Undergraduates in ${subset}`} {getCount(_.map(years, _.capitalize))}
                    </li>);
                  }
                } else {
                  renderedAudience.push(<li key={audienceType}>
                    {`All Undergraduates in ${subset}`} {getCount(['All'])}
                  </li>);
                }
              }
              break;
            default: {
              const group = _.replace(audienceType, 'Dept:', '');
              const groupDisplayName = _.startCase(group);
              if (isUniWide || !_.isEmpty(dept.name)) {
                renderedAudience.push(<li key={audienceType}>
                  {`All ${groupDisplayName} in ${_.startsWith(audienceType, 'Dept:') ? dept.name : 'the University'}`} {getCount([group])}
                </li>);
              }
            }
          }
        });
      }
    }

    return renderedAudience;
  }

  render() {
    const { baseAudience, fetching, error } = this.state;
    const { itemType, canEstimateAudience } = this.props;

    if (this.state.public) {
      return (
        <div className="well well-sm">
          <div className="pull-right">
            <p className="lead">
              <i className="fal fa-globe-europe"></i> This {itemType} will be public
            </p>
          </div>
        </div>
      );
    }

    const list = _.compact(this.parseAudienceComponents());
    const renderedList = list.length > 0
      ? list : <li>Build your audience using options on the left</li>;

    const renderedError = error
      || (canEstimateAudience ? null : 'You need to specify both target audience and tag(s)');

    return (
      <div className="well well-sm">
        <div className="pull-right">
          <i
            className="fal fa-info-circle"
            data-toggle="tooltip"
            data-placement="left"
            title="Estimated audience size will be shown here, when audience and categories
        have been selected"
          />
        </div>
        <p className="lead">
          {AudienceIndicator.totaliser(renderedError, fetching, baseAudience)}
        </p>
        <ul className="audience-component-list">{renderedList}</ul>
      </div>
    );
  }
}

function select(store) {
  return {
    audienceComponents: store.audience,
    canEstimateAudience: store.canEstimateAudience,
  };
}

export default connect(select)(AudienceIndicator);
