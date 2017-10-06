import AudienceIndicator from 'publish/components/AudienceIndicator';
import * as React from 'react';
import {render} from 'enzyme';

describe('AudienceIndicator', () => {

  it('handles \'everyone in department\' case', () => {
    const props = {
      audienceComponents: {
        department: {name: 'School of Bodybuilding'},
        audience: {
          department: 'Dept:All'
        }
      }
    };

    const render = render(<AudienceIndicator {...props}/>);

    expect(render).to.contain('Everyone in School of Bodybuilding');
  });


  it('does some other shit', () => {
    const audienceComponents = {
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


  });


  it('handles null audience components', () => {
    // some shiz
  })

});