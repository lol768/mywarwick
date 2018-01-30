import { TimetableAlarmsView } from 'components/views/settings/TimetableAlarmsView';
// import { expect } from 'chai';

describe('timing phrasing helper', () =>{
  it('should render minutes properly', () => {
    const expected = '15 minutes before';
    const actual = TimetableAlarmsView.getDescriptionForTiming(15);
   expect(actual).to.equal(expected);
  });

  it('should render hours properly', () => {
    const expected = '1 hour before';
    const actual = TimetableAlarmsView.getDescriptionForTiming(60);
   expect(actual).to.equal(expected);
  });

  it('should render hours properly', () => {
    const expected = '2 hours before';
    const actual = TimetableAlarmsView.getDescriptionForTiming(120);
    expect(actual).to.equal(expected);
  });

  it('should render hours and minutes properly', () => {
    const expected = '1 hour 22 minutes before';
    const actual = TimetableAlarmsView.getDescriptionForTiming(82);
    expect(actual).to.equal(expected);
  });

  it('should render hours and minutes properly', () => {
    const expected = '1 hour 1 minute before';
    const actual = TimetableAlarmsView.getDescriptionForTiming(61);
    expect(actual).to.equal(expected);
  });

});