import { AudienceIndicator } from 'publish/components/AudienceIndicator';
import * as React from 'react';
import * as enzyme from 'enzyme';

describe('AudienceIndicator', () => {

  it('handles \'everyone in department\' case', () => {
    const props = {
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        }
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props} />);

    expect(render.html()).to.contain('Everyone in School of Bodybuilding');
  });


  it('renders readable ', () => {
    const props = {
      audienceComponents: {
        department: { name: 'Anatomy and Physiology' },
        audience: {
          department: {
            groups: {
              'Dept:TaughtPostgrads': undefined,
              'Dept:TeachingStaff': undefined,
              modules: [
                {
                  value: 'CS118',
                  text: 'CS118: Programming for Computer Scientists'
                },
                {
                  value: 'CS101',
                  text: 'CS101: Introduction to the semi-colon'
                }
              ],
              seminarGroups: [
                {
                  text: 'Tutorial Group 2: CH160 Tutorials'
                },
                {
                  text: 'Tutorial Group 2B: Or maybe 2A'
                }
              ],
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
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props} />);
    const html = render.html();

    [
      'Supervisees of Dirk Diggler (Anatomy and Physiology)',
      'Tutorial Group 2: CH160 Tutorials',
      'Tutorial Group 2B: Or maybe 2A',
      'CS118: Programming for Computer Scientists',
      'CS101: Introduction to the semi-colon',
      'All Taught Postgrads in Anatomy and Physiology',
      'All Teaching Staff in Anatomy and Physiology',
      '2 usercodes',
    ].forEach(readableComponent => expect(html).to.contain(readableComponent))

  });

  it('displays nothing for no audience', () => {
    const props = {
      audienceComponents: {
        department: undefined
      },
    };

    const render = enzyme.render(<AudienceIndicator {...props} />);

    expect(render.find('.audience-component-list').first().text()).to.equal('');

  });

  it('handles \'everyone in university\' case', () => {
    const props = {
      audienceComponents: {
        audience: {
          universityWide: {
            groups: {
              TeachingStaff: undefined,
              UndergradStudents: undefined
            }
          }
        }
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props}/>);

    expect(render.html()).to.contain('All Teaching Staff in the University');
    expect(render.html()).to.contain('All Undergrad Students in the University');
  });


  it('don\'t render \'undefined\' as department name', () => {
    const props = {
      audienceComponents: {
        department: {},
        audience: {
          department: {
            groups: {
              TeachingStaff: undefined,
              UndergradStudents: undefined
            }
          }
        }
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props}/>);

    expect(render.html()).to.not.contain('All Teaching Staff');
    expect(render.html()).to.not.contain('All Undergrad Students');
  });

});
