export const now = new Date('2016-05-19T13:00:00+01:00');
export const end = new Date('2016-05-19T14:00:00+01:00');

export const ITEMS = {
  firstEvent: {
    id: '1',
    title: 'First Event',
    start: now.toISOString(),
    end: end.toISOString(),
    location: {
      name: 'Location'
    },
    organiser: {
      name: 'John Smith'
    },
    academicWeek: 0,
  },
  secondEvent: {
    id: '2',
    title: 'Second Event',
    start: end.toISOString(),
    end: end.toISOString(),
    location: {
      name: 'Location'
    },
    organiser: {
      name: 'John Smith'
    },
    academicWeek: 1
  },
  parentOnly: {
    id: '2',
    parent: {
      shortName: "IN101",
      fullName: "Introduction to IT"
    },
    start: end.toISOString(),
    end: end.toISOString(),
    location: {
      name: 'Location'
    },
    organiser: {
      name: 'John Smith'
    },
  },
  twoWeeker: {
    id: 'xyz',
    start: '2016-05-16T00:00:00Z',
    end: '2016-05-28T00:00:00Z',
    isAllDay: true,
    title: 'Two-week event'
  },
  singleDayAllDay: {
    id: '3',
    start: '2016-06-16T00:00:00Z',
    isAllDay: true,
    title: 'One day event'
  }
};