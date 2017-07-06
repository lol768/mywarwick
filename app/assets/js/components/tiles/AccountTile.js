import React from 'react';
import * as PropTypes from 'prop-types';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import Hyperlink from '../ui/Hyperlink';
import AccountPhoto from '../ui/AccountPhoto';
import moment from 'moment';
import * as dateFormats from '../../dateFormats';
import _ from 'lodash-es';

export default class AccountTile extends TileContent {

  static propTypes = {
    content: PropTypes.shape({
      fullName: PropTypes.string.isRequired,
      email: PropTypes.string.isRequired,
      userId: PropTypes.string.isRequired,
      universityId: PropTypes.string.isRequired,
      homeDepartment: PropTypes.shape({
        code: PropTypes.string.isRequired,
        name: PropTypes.string.isRequired,
      }).isRequired,
      userType: PropTypes.string.isRequired,
      jobTitle: PropTypes.string,
      inactivationDate: PropTypes.string,
      phoneNumber: PropTypes.string,
      studentCourseDetails: PropTypes.arrayOf(PropTypes.shape({
        course: PropTypes.shape({
          name: PropTypes.string.isRequired,
        }).isRequired,
        currentRoute: PropTypes.shape({
          code: PropTypes.string.isRequired,
          name: PropTypes.string.isRequired,
        }).isRequired,
        levelCode: PropTypes.string.isRequired,
        mostSignificant: PropTypes.bool.isRequired,
      })),
    }).isRequired,
    user: PropTypes.shape({
      photo: PropTypes.shape({
        url: PropTypes.string,
      }),
      name: PropTypes.string.isRequired,
    }).isRequired,
  };

  static canZoom() {
    return true;
  }

  static getLink() {
    return (
      <div className="bottom-right">
        <Hyperlink href="http://warwick.ac.uk/myaccount" className="text--dotted-underline">
          Account settings
        </Hyperlink>
      </div>
    );
  }

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES.concat([TILE_SIZES.LARGE]);
  }

  static realInactivationDate(dateString) {
    if (dateString) {
      const date = moment(dateString);
      if (date.isAfter(moment().add(1000, 'years'))) {
        return null;
      }
      return <li>{ `Expected end date ${dateFormats.formatDateMoment(date)}` }</li>;
    }
    return null;
  }

  isEmpty() {
    return false;
  }

  getSmallBody() {
    const member = this.props.content;
    return (
      <div>
        <div>{ member.fullName }</div>
        <div>{ member.email }</div>
        <div>{ `${member.userId}, ${member.universityId}` }</div>
        <div>{ `${member.userType}, ${member.homeDepartment.name}` }</div>
        { AccountTile.getLink() }
      </div>
    );
  }

  getWideBody() {
    const member = this.props.content;
    const user = this.props.user;
    return (
      <div className="media">
        <div className="media-left">
          { user.photo && user.photo.url &&
            <AccountPhoto user={ user } className="media-object media-object-img-fix" />
          }
        </div>
        <div className="media-body">
          <div>{ member.fullName }</div>
          <div>{ member.email }</div>
          <div>{ `${member.userId}, ${member.universityId}` }</div>
          <div>
            { (member.jobTitle) ? `${member.jobTitle}, ` : null }
            { `${member.userType}, ${member.homeDepartment.name}` }
          </div>
          { AccountTile.getLink() }
        </div>
      </div>
    );
  }

  getLargeBody() {
    const member = this.props.content;
    const user = this.props.user;
    let scd = undefined;
    if (member.studentCourseDetails) {
      scd = _.find(member.studentCourseDetails, c => c.mostSignificant);
      if (scd === undefined) {
        scd = _.last(member.studentCourseDetails);
      }
    }

    return (
      <div className="media">
        <div className="media-left">
          { user.photo && user.photo.url &&
          <AccountPhoto user={ user } className="media-object media-object-img-fix" />
          }
        </div>
        <div className="media-body">
          <ul className="list-unstyled">
            <li>{ member.fullName }</li>
            <li>{ member.email }</li>
            <li>{ `${member.userId}, ${member.universityId}` }</li>
            <li>
              { (member.jobTitle) ? `${member.jobTitle}, ` : null }
              { `${member.userType}, ${member.homeDepartment.name}` }
            </li>
            { AccountTile.realInactivationDate(member.inactivationDate) }
            { member.phoneNumber && <li><i className="fa fa-phone" /> { member.phoneNumber }</li> }
            { (scd) && <li>Course: { scd.course.name }</li> }
            { (scd) &&
              <li>Route: { scd.currentRoute.code.toUpperCase() } { scd.currentRoute.name }</li>
            }
            { (scd) && <li>Year of study: { scd.levelCode }</li> }
          </ul>
          { AccountTile.getLink() }
        </div>
      </div>
    );
  }

}
