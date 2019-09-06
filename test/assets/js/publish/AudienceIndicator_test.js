import { AudienceIndicator } from 'publish/components/AudienceIndicator';
import * as React from 'react';
import * as enzyme from 'enzyme';

describe('AudienceIndicator', () => {

  const baseProps = {
    canEstimateAudience: true,
    itemType: 'alert',
    promiseSubmit: () => {},
  };

  it('makes correct totaliser for error', () => {
    const res = enzyme.shallow(AudienceIndicator.totaliser('It has all gone wrong', false, 0));
    res.html().should.contain('<i class="fal fa-user-slash"></i>');
    res.text().should.contain('It has all gone wrong');
  });

  it('makes correct totaliser for fetching', () => {
    const res = enzyme.shallow(AudienceIndicator.totaliser(null, true, 0));
    res.html().should.contain('<i class="fal fa-spin fa-sync"></i>');
    res.text().should.contain('Recalculating user estimate…');
  });

  it('makes correct totaliser when user got no audience', () => {
    const res = enzyme.shallow(AudienceIndicator.totaliser(null, false, 0));
    res.html().should.contain('<i class="fal fa-user-slash"></i>');
    res.text().should.contain('No matching users');
  });

  it('makes correct totaliser when user got single user audience', () => {
    const res = enzyme.shallow(AudienceIndicator.totaliser(null, false, 1));
    res.html().should.contain('<i class="fal fa-user"></i>');
    res.text().should.contain('One matching user');
  });

  it('makes correct totaliser when user got multi-user audience', () => {
    const res = enzyme.shallow(AudienceIndicator.totaliser(null, false, 42));
    res.html().should.contain('<i class="fal fa-users"></i>');
    res.text().should.contain('42 matching users');
  });

  it('renders hint block properly when can estimate', () => {
    const render = enzyme.shallow(<AudienceIndicator {...{
      ...baseProps,
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        },
      },
    }} />);
    render.text().should.contain('Everyone in School of Bodybuilding');
  });

  it('renders hint block properly when cannot estimate', () => {
    const render = enzyme.shallow(<AudienceIndicator {...{
      ...baseProps,
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { 'Dept:All': undefined }
        },
      },
      canEstimateAudience: false,
    }} />);
    render.text().should.contain('You need to specify both target audience and tag(s)');
  });

  it('updates audience component list', () => {
    const render = enzyme.shallow(<AudienceIndicator {...{
      ...baseProps,
      audienceComponents: {
        department: { name: 'School of Bodybuilding' },
        audience: {
          department: { groups: {'Dept:Staff': undefined, 'Dept:TaughtPostgrads': undefined} }
        },
      },
     }} />);
    render.html().should.contain('All Staff in School of Bodybuilding');
    render.html().should.contain('All Taught Postgrads in School of Bodybuilding');
    render.html().should.not.contain('All Research Postgrads in School of Bodybuilding');

    render.setProps({ audienceComponents: { department: { name: 'School of Bodybuilding' }, audience: { department: { groups: {'Dept:ResearchPostgrads': undefined}}}}})
      .html().should.contain('All Research Postgrads in School of Bodybuilding');

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

    render.html().should.contain('Everyone in School of Bodybuilding');
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
    const groupedAudience = {
      TaughtPostgrads: 12,
    };
    // assign usercode with array-bracket notation: the key generated by Scala has illegal chars
    groupedAudience['UsercodesAudience(Set(Usercode(cusjau),Usercode(u1234567)))'] = 2;
    render.setState({ groupedAudience });
    const html = render.html();

    [
      'Supervisees of Dirk Diggler (Anatomy and Physiology) <span class="badge">0</span>',
      'Personal Tutees of Dirk Diggler (Anatomy and Physiology) <span class="badge">0</span>',
      'Tutorial Group 2: CH160 Tutorials <span class="badge">0</span>',
      'Tutorial Group 2B: Or maybe 2A <span class="badge">0</span>',
      'CS118: Programming for Computer Scientists <span class="badge">0</span>',
      'CS101: Introduction to the semi-colon <span class="badge">0</span>',
      'All Taught Postgrads in Anatomy and Physiology <span class="badge">12</span>',
      'All Teaching Staff in Anatomy and Physiology <span class="badge">0</span>',
      'Usercodes or university IDs <span class="badge">2</span>',
    ].forEach(readableComponent => html.should.contain(readableComponent))

  });

  it('displays nothing for no audience', () => {
    const props = {
      ...baseProps,
      audienceComponents: {
        department: undefined
      },
    };

    const render = enzyme.render(<AudienceIndicator {...props} />);

    render.find('.audience-component-list').first().text().should.equal('Build your audience using options on the left');

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

    render.html().should.contain('All Teaching Staff in the University');
    render.html().should.contain('All Undergrad Students in the University');
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

    render.html().should.not.contain('All Teaching Staff');
    render.html().should.not.contain('All Undergrad Students');
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

    render.html().should.contain(`All first, second, and final year Undergraduates in ${props.audienceComponents.department.name} <span class="badge">20</span>`)
  })

});
