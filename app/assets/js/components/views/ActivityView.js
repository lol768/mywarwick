import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';

import groupItemsByDate from '../../GroupItemsByDate';

export default class ActivityView extends ReactComponent {

  render() {
    return (
      <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
        <ActivityItem key="a"
                      text="You changed your preferred photo"
                      source="Photos"
                      date="2015-10-12T12:00"/>
        <ActivityItem key="b"
                      text="You submitted coursework for LA118 Intro to Criminal Law"
                      source="Tabula"
                      date="2015-10-11T09:10"/>
        <ActivityItem key="c"
                      text="You signed in using Edge on Windows 10"
                      source="Web Sign-On"
                      date="2015-09-14T14:36"/>
      </GroupedList>
    );
  }

}

