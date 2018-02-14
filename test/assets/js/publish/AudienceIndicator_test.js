import { AudienceIndicator } from 'publish/components/AudienceIndicator';
import * as React from 'react';
import * as enzyme from 'enzyme';

describe('AudienceIndicator', () => {

  const baseProps = {
    hint: {
      text: "the ultimate hint text, go figure.",
      link: "https://warwick.ac.uk",
    },
  };

  it('makes correct help text when user did not choose any audience', () => {
    expect(enzyme
      .shallow(AudienceIndicator.makePeopleInTotalText(0, {}))
      .html())
      .to.equal('<em>(Waiting for all options to be selected…)</em>');

    expect(enzyme
      .shallow(AudienceIndicator.makePeopleInTotalText(0, null))
      .html())
      .to.equal('<em>(Waiting for all options to be selected…)</em>');

  });

  it('makes correct help text when user did choose some audience, but resulted in 0 audience', () => {
    expect(enzyme
      .shallow(AudienceIndicator.makePeopleInTotalText(0, { group1: 0 }))
      .html())
      .to.equal('<div>0 people in current selection</div>');
  });

  it('renders hint block properly', () => {
    const render = enzyme.shallow(<AudienceIndicator {...{
      ...baseProps,
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        },
      },
    }} />);
    expect(render.html()).to.contain('the ultimate hint text, go figure. ');
    expect(render.html()).to.contain("\<a href=\"https://warwick.ac.uk\" target=\"_blank\"\>More info…\<\/a\>");
  });

  it('renders hint block without link properly', () => {
    const render = enzyme.shallow(<AudienceIndicator {...{
      hint: {
        text: "the ultimate hint text, go figure.",
      },
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        },
      },
    }} />);
    expect(render.html()).to.contain('the ultimate hint text, go figure.');
  });

  it('handles \'everyone in department\' case', () => {
    const props = {
      ...baseProps,
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        },
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props} />);

    expect(render.html()).to.contain('Everyone in School of Bodybuilding');
  });


  it('renders readable ', () => {
    const props = {
      ...baseProps,
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
              staffRelationships: [
                {
                  text: 'Dirk Diggler (Anatomy and Physiology)',
                  options: [{
                    supervisor: {
                      studentRole: 'supervisee',
                      selected: true
                    }
                  }]
                },
                {
                  text: 'Dirk Diggler (Anatomy and Physiology)',
                  options: [{
                    personalTutor: {
                      studentRole: 'personal tutee',
                      selected: true
                    }
                  }]
                }
              ],
              listOfUsercodes: ['cusjau', 'u1234567']
            }
          }
        }
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props} />);
    render.setState({
      groupedAudience: {
        TaughtPostgrads: 12,
      }
    });
    const html = render.html();

    [
      'Supervisees of Dirk Diggler (Anatomy and Physiology): 0 people',
      'Personal Tutees of Dirk Diggler (Anatomy and Physiology): 0 people',
      'Tutorial Group 2: CH160 Tutorials: 0 people',
      'Tutorial Group 2B: Or maybe 2A: 0 people',
      'CS118: Programming for Computer Scientists: 0 people',
      'CS101: Introduction to the semi-colon: 0 people',
      'All Taught Postgrads in Anatomy and Physiology: 12 people',
      'All Teaching Staff in Anatomy and Physiology: 0 people',
      'Usercodes or university IDs: 2 people',
    ].forEach(readableComponent => expect(html).to.contain(readableComponent))

  });

  it('displays nothing for no audience', () => {
    const props = {
      ...baseProps,
      audienceComponents: {
        department: undefined
      },
    };

    const render = enzyme.render(<AudienceIndicator {...props} />);

    expect(render.find('.audience-component-list').first().text()).to.equal('');

  });

  it('handles \'everyone in university\' case', () => {
    const props = {
      ...baseProps,
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
      ...baseProps,
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

  it('groups undergraduate subsets and combines audience count', () => {
    const props = {
      ...baseProps,
      audienceComponents: {
        department: { name: 'Anatomy and Physiology' },
        audience: {
          department: {
            groups: {
              undergraduates: {
                year: {
                  'Dept:UndergradStudents:First': undefined,
                  'Dept:UndergradStudents:Second': undefined,
                  'Dept:UndergradStudents:Final': undefined,
                }
              }
            }
          }
        }
      },
    };

    const render = enzyme.shallow(<AudienceIndicator {...props} />);
    render.setState({
      groupedAudience: {
        First: 2,
        Second: 7,
        Final: 11,
      }
    });

    expect(render.html()).to.contain(`All first, second, and final year Undergraduates in ${props.audienceComponents.department.name}: 20 people`)
  })

});
