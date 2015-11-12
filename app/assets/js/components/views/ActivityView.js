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
                      date={moment().subtract(1, 'days')}/>
        <ActivityItem key="b"
                      text="You submitted coursework for LA118 Intro to Criminal Law"
                      source="Tabula"
                      date={moment().add(1, 'days')}/>
        <ActivityItem key="c"
                      text="You signed in using Edge on Windows 10"
                      source="Web Sign-On"
                      date={moment().subtract(2, 'months')}/>
      </GroupedList>
    );
  }

}

