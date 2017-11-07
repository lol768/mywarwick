import React from 'react';
import * as PropTypes from 'prop-types';
import moment from 'moment';
import _ from 'lodash-es';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import Hyperlink from '../ui/Hyperlink';
import AccountPhoto from '../ui/AccountPhoto';
import * as dateFormats from '../../dateFormats';
import { signOut } from '../../userinfo';

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
      userSource: PropTypes.string,
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
    }),
    user: PropTypes.shape({
      photo: PropTypes.shape({
        url: PropTypes.string,
      }),
      name: PropTypes.string,
    }).isRequired,
  };

  static canZoom() {
    return true;
  }

  static getLink(member) {
    return (
      <div>
        {member.fullName}&nbsp;
        <Hyperlink href="//warwick.ac.uk/myaccount" className="text--dotted-underline">
          <small>(Settings)</small>
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
      return `Expected end date: ${dateFormats.formatDateMoment(date)}`;
    }
    return null;
  }

  static getMediaLeft(user) {
    return (
      <div className="media-left">
        {user.photo && user.photo.url &&
        <Hyperlink href="//photos.warwick.ac.uk/yourphoto">
          <AccountPhoto user={user} className="media-object media-object-img-fix" />
        </Hyperlink>
        }
      </div>
    );
  }

  static getSCD(member) {
    if (member.studentCourseDetails) {
      const scd = _.find(member.studentCourseDetails, c => c.mostSignificant);
      if (scd === undefined) {
        return _.last(member.studentCourseDetails);
      }
      return scd;
    }
    return null;
  }

  isEmpty() {
    return false;
  }

  static makeFullName(member) {
    return AccountTile.makeLineItem(
      AccountTile.getLink(member),
      'fa-user-o',
    );
  }

  static makeEmail(member) {
    return AccountTile.makeLineItem(
      member.email,
      'fa-envelope-o',
    );
  }

  static makeUserid(member) {
    return AccountTile.makeLineItem(
      `${member.userId}, ${member.universityId}`,
      'fa-id-card-o',
    );
  }

  static makeUserType(member) {
    const scd = AccountTile.getSCD(member);
    if (scd) {
      return AccountTile.makeLineItem(
        `${member.userType}, Course: ${scd.course.name}`,
        'fa-university',
      );
    }
    return AccountTile.makeLineItem(
      `${member.userType}, ${member.homeDepartment.name}`,
      'fa-address-book-o',
    );
  }

  static makeJobTitle(member) {
    if (member.jobTitle) {
      return AccountTile.makeLineItem(
        member.jobTitle,
        'fa-id-badge',
      );
    }
    return null;
  }

  static makePhone(member) {
    if (member.phoneNumber) {
      return AccountTile.makeLineItem(
        member.phoneNumber,
        'fa-phone',
      );
    }
    return null;
  }

  static makeEndDate(member) {
    const date = AccountTile.realInactivationDate(member.inactivationDate);
    if (date) {
      return AccountTile.makeLineItem(
        date,
        'fa-graduation-cap',
      );
    }
    return null;
  }

  static makeRoute(member) {
    const scd = AccountTile.getSCD(member);
    if (scd) {
      return AccountTile.makeLineItem(
        `Route: ${scd.currentRoute.code.toUpperCase()} ${scd.currentRoute.name}`,
        'fa-map-o',
      );
    }
    return null;
  }

  static makeYearOfStudy(member) {
    const scd = AccountTile.getSCD(member);
    if (scd) {
      return AccountTile.makeLineItem(
        `Year of study: ${scd.levelCode}`,
        'fa-calendar-o',
      );
    }
    return null;
  }

  static makeHomeDepartment(member) {
    const scd = AccountTile.getSCD(member);
    if (scd) {
      return AccountTile.makeLineItem(
        `Home department: ${member.homeDepartment.name}`,
        'fa-home',
      );
    }
    return null;
  }

  static makeLineItem(content, icon) {
    return (
      <li>
        <i className={`fa fa-li fa-fw ${icon}`} />
        { content }
      </li>
    );
  }

  getSmallBody() {
    const member = this.props.content;
    return (
      <ul className="list-unstyled fa-ul">
        {AccountTile.makeFullName(member)}
        {AccountTile.makeEmail(member)}
        {AccountTile.makeUserid(member)}
        {AccountTile.makeUserType(member)}
      </ul>
    );
  }

  getWideBody() {
    const member = this.props.content;
    const user = this.props.user;
    return (
      <div className="media">
        {AccountTile.getMediaLeft(user)}
        <div className="media-body">
          <ul className="list-unstyled fa-ul">
            {AccountTile.makeFullName(member)}
            {AccountTile.makeEmail(member)}
            {AccountTile.makeUserid(member)}
            {AccountTile.makeUserType(member)}
          </ul>
        </div>
      </div>
    );
  }

  getLargeBody() {
    const member = this.props.content;
    const user = this.props.user;

    return (
      <div className="media">
        {AccountTile.getMediaLeft(user)}
        <div className="media-body">
          <ul className="list-unstyled fa-ul">
            {AccountTile.makeFullName(member)}
            {AccountTile.makeEmail(member)}
            {AccountTile.makeUserid(member)}
            {AccountTile.makeJobTitle(member)}
            {AccountTile.makeUserType(member)}
            {AccountTile.makeEndDate(member)}
            {AccountTile.makePhone(member)}
            {AccountTile.makeRoute(member)}
            {AccountTile.makeYearOfStudy(member)}
            {AccountTile.makeHomeDepartment(member)}
            <li>&nbsp;</li>
            {member.userSource === 'WBSLdap' && // user has signed in with WBS credentials
            <li>
              You’re signed in with your WBS account. To access all the features of My Warwick,
              please&nbsp;
              <a
                role="button"
                tabIndex={0}
                className="text--dotted-underline"
                onClick={signOut}
              >
                sign in with your ITS credentials instead.
              </a>
            </li>}
          </ul>
        </div>
      </div>
    );
  }
}
