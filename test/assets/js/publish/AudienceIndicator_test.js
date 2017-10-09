import AudienceIndicator from 'publish/components/AudienceIndicator';
import * as React from 'react';
import * as enzyme from 'enzyme';

const mockStore = state => ({
  getState() {
    return state;
  },
  dispatch() {
  },
  subscribe() {
  },
});

const shallowWithStore = (component, store) => enzyme.shallow(component, { context: { ...store } });

describe('AudienceIndicator', () => {

  it('handles \'everyone in department\' case', () => {
    const audienceState = {
      audience: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        }
      }
    };

    const store = mockStore(audienceState);
    const render = shallowWithStore(<AudienceIndicator store={store} promiseSubmit={() => {
    }}/>, store);

    expect(render.html()).to.contain('Everyone in School of Bodybuilding');
  });


  it('renders readable ', () => {
    const audienceState = {
      audience: {
        department: { name: 'Anatomy and Physiology' },
        audience: {
          department: {
            groups: {
              'Dept:TaughtPostgrads': undefined,
              'Dept:TeachingStaff': undefined,
              modules: [{
                value: 'CS118',
                text: 'CS118: Programming for Computer Scientists'
              }],
              seminarGroups: [{
                text: 'Tutorial Group 2: CH160 Tutorials'
              }],
              staffRelationships: [{
                text: 'Dirk Diggler (Anatomy and Physiology)',
                options: [{
                  supervisor: {
                    studentRole: 'supervisee',
                    selected: true
                  }
                }]
              }],
              listOfUsercodes: ['cusjau', 'u1234567']
            }
          }
        }
      }
    };

    const store = mockStore(audienceState);
    const render = shallowWithStore(<AudienceIndicator store={store} promiseSubmit={() => {
    }}/>, store);
    const html = render.html();

    [
      'Supervisees of Dirk Diggler (Anatomy and Physiology)',
      'Tutorial Group 2: CH160 Tutorials',
      'CS118: Programming for Computer Scientists',
      'All Taught Postgrads in Anatomy and Physiology',
      'All Teaching Staff in Anatomy and Physiology',
      '2 usercodes',
    ].forEach(readableComponent => expect(html).to.contain(readableComponent))

  });

  it('displays nothing for no audience', () => {
    const audienceState = {
      audience: {
        department: undefined
      }
    };

    const store = mockStore(audienceState);
    const render = enzyme.render(<AudienceIndicator store={store} promiseSubmit={() => {
    }}/>);

    expect(render.find('.audience-component-list').first().text()).to.equal('');

  });

  it('handles \'everyone in university\' case', () => {
    const audienceState = {
      audience: {
        audience: {
          universityWide: {
            groups: {
              TeachingStaff: undefined,
              UndergradStudents: undefined
            }
          }
        }
      }
    };

    const store = mockStore(audienceState);
    const render = shallowWithStore(<AudienceIndicator store={store} promiseSubmit={() => {
    }}/>, store);

    expect(render.html()).to.contain('All Teaching Staff in the University');
    expect(render.html()).to.contain('All Undergrad Students in the University');
  });
});
