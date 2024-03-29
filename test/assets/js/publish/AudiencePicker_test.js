import { AudiencePicker } from 'publish/components/AudiencePicker';
import { RadioButton, Checkbox } from '../../../../app/assets/js/components/ui/Checkbox';
import * as React from 'react';
import * as enzyme from 'enzyme';
import _ from 'lodash-es';

const context = {
  store: {
    dispatch: () => {},
  },
};

describe('AudiencePicker', () => {

  const deptSubsetOpts = {'TeachingStaff':'Teaching Staff','AdminStaff':'Administrative Staff','UndergradStudents':'Undergraduates','TaughtPostgrads':'Taught Postgraduates','ResearchPostgrads':'Research Postgraduates'};
  const locationOpts = {'CentralCampusResidences':'Central campus residences','WestwoodResidences':'Westwood residences','Coventry':'Coventry','Kenilworth':'Kenilworth','LeamingtonSpa':'Leamington Spa'};

  it('shows single \'hidden\' input if single department', () => {
    const props = {
      isGod: false,
      departments: {
        FU: {
          name: 'Fun Department'
        },
      },
      
    };

    const shallow = enzyme.shallow(<AudiencePicker {...props} />);

    expect(shallow.state('department').name).to.eql(props.departments.FU.name);

    const deptInput = shallow.find('input[name="audience.department"]').first();
    expect(deptInput.type()).to.eql('input');
    expect(deptInput.is('[hidden]')).to.eql(true);
    expect(deptInput.is('[readOnly]')).to.eql(true);
    expect(deptInput.prop('value')).is.eql('FU');
  });


  it('shows input select or multiple departments', () => {
    const ELLIPSIS = '…';

    const props = {
      isGod: false,
      departments: {
        FU: { name: 'Fun Department' },
        MU: { name: 'Agriculture Department' },
        PU: { name: 'Sewage Department' },
      },
      
    };

    const shallow = enzyme.shallow(<AudiencePicker {...props} />);

    expect(shallow.state().department).to.eql(ELLIPSIS);

    const deptSelect = shallow.find('select[name="audience.department"]').first();

    expect(deptSelect.type()).to.eql('select');
    expect(deptSelect.prop('defaultValue')).to.eql('');
    expect(deptSelect.children().length).to.eql(4); // <option> * 3 + the 'Select a department' one

    const deptOptions = deptSelect.children().map(node => node.prop('value'));
    expect(deptOptions).to.eql(['', ...Object.keys(props.departments)]); // including <option> with '' value (as above)

  });

  it('renders university-wide view when props.isGod', () => {
    const props = {
      isGod: true,
      departments: {
        FU: { name: 'Fun Department' },
        MU: { name: 'Agriculture Department' },
        PU: { name: 'Sewage Department' },
      },
      
    };
    const shallow = enzyme.shallow(<AudiencePicker {...props}/>);

    const radioButtons = shallow.children({ className: 'list-group' }).first().children().at(1);

    expect(shallow.children().length).to.eql(2);
    expect(radioButtons.children().first().prop('label')).to.eql('People across the whole university');
    expect(radioButtons.children().at(1).prop('label')).to.eql('People within a particular department');
  });

  it('populates update from props.formData and serialises form values correctly', () => {
    const formData = {
      department: 'FU',
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
              value: '12d779c3-aafd-4883-9c1d-cacfdba37974',
              text: 'Tutorial Group 2: CH160 Tutorials'
            }],
            staffRelationships: [{
              value: '1234567',
              text: 'Dirk Diggler (Anatomy and Physiology)',
              options: [{
                supervisor: {
                  agentRole: 'supervisor',
                  studentRole: 'supervisee',
                  students: ['Dennis', 'Trudy'],
                  selected: true
                }
              }]
            }],
            listOfUsercodes: ['cusjau', 'u1234567']
          }
        }
      }
    };

    const props = {
      isGod: false,
      formData,
      departments: {
        FU: { name: 'Fun Department' },
        MU: { name: 'Agriculture Department' },
      },
      deptSubsetOpts,
      locationOpts,
      
    };

    const html = enzyme.render(<form><AudiencePicker {...props} /></form>);
    const serializedForm = html.children().first().serializeArray();
    const audienceValues = _.reduce(serializedForm, (acc, o) =>
        o.name === 'audience.audience[]' ?
          [o.value, ...acc] : acc
      , []);

    const expectedAudienceValues = [
      'Dept:cusjau\r\nu1234567',
      'Dept:Relationship:supervisor:1234567',
      'Dept:SeminarGroup:12d779c3-aafd-4883-9c1d-cacfdba37974',
      'Dept:Module:CS118',
      'Dept:TaughtPostgrads',
      'Dept:TeachingStaff'];

    expect(audienceValues).to.have.length(expectedAudienceValues.length);
    expect(audienceValues.join()).to.contain(expectedAudienceValues.join())
  });


  it('displays child checkboxes/radios when parent is selected', () => {
    const props = {
      isGod: false,
      departments: {
        MU: { name: 'Agriculture Department' },
      },
      deptSubsetOpts,
      locationOpts,
      audienceDidUpdate: () => {},
    };

    const mounted = enzyme.mount(<AudiencePicker {...props} />,  { context });
    const btnOne = mounted.find(RadioButton).at(1);
    expect(btnOne.children()).to.have.length(1);

    btnOne.find('input').first().simulate('change');

    expect(btnOne.children()).to.have.length(2);
    expect(btnOne.children().at(1).find(Checkbox)).to.have.length(10)

  });

  it('displays module picker input when checkbox selected', () => {
    const props = {
      isGod: false,
      departments: {
        MU: { name: 'Agriculture Department' },
      },
      audienceDidUpdate: () => {},
    };

    const stateOne = {
      department: { code: 'MU', name: 'Agriculture Department' },
      audience: { department: { groups: undefined } }
    };

    const stateTwo = {
      department: { code: 'MU', name: 'Agriculture Department' },
      audience: { department: { groups: { modules: undefined } } }
    };

    const shallow = enzyme.shallow(<AudiencePicker {...props} />,  { context });

    shallow.setState(stateOne);
    expect(shallow.find(Checkbox).find({ value: 'modules' }).first().prop('isChecked')).to.eql(false);
    expect(shallow.find(Checkbox).find({ value: 'modules' }).first().html()).to.not.contain('Start typing to find a module');

    shallow.setState(stateTwo);
    expect(shallow.find(Checkbox).find({ value: 'modules' }).first().prop('isChecked')).to.eql(true);
    expect(shallow.find(Checkbox).find({ value: 'modules' }).first().html()).to.contain('Start typing to find a module');
  });

  it('deselects other radio buttons in group when single button selected', () => {
    const props = {
      isGod: false,
      departments: {
        MU: { name: 'Agriculture Department' },
      },
      audienceDidUpdate: () => {},
    };

    const newState = {
      locations: {
        yesLocation: undefined,
      },
    };

    const shallow = enzyme.shallow(<AudiencePicker {...props} />,  { context });

    expect(shallow.find('.list-group').at(1).find(RadioButton).first().prop('isChecked')).to.eql(true);
    expect(shallow.find('.list-group').at(1).find(RadioButton).at(1).prop('isChecked')).to.eql(false);

    shallow.setState(newState);
    expect(shallow.find('.list-group').at(1).find(RadioButton).first().prop('isChecked')).to.eql(false);
    expect(shallow.find('.list-group').at(1).find(RadioButton).at(1).prop('isChecked')).to.eql(true);
  });

  it('only displays \'listofUsercodes\' group for non-teaching departments', () => {
    const props = {
      isGod: false,
      departments: {
        MU: { name: 'Agriculture Department', faculty: 'X' }, // faculty:X indicates non-teaching dept
      },
      formData : {
        audience: { department: { groups: { listOfUsercodes: [] } } },
        department: 'MU'
      },
      audienceDidUpdate: () => {},
    };

    const mounted = enzyme.mount(<AudiencePicker {...props} />,  { context });

    expect(mounted.find('.list-group').first().find(RadioButton).length).to.eql(2);
    expect(mounted.find('.list-group').first().find(RadioButton).at(1).props().label).to.contain('Groups in Agriculture Department');
    expect(mounted.find('.list-group').first().find(RadioButton).at(1).find(Checkbox).props().label).to.contain('A list of people I\'ll type or paste in');

    mounted.setState({ department: { code: 'FU', name: 'Fun Department', faculty: 'Arts' } });
    expect(mounted.find('.list-group').first().find(RadioButton).length).to.eql(2);
    expect(mounted.find('.list-group').first().find(RadioButton).at(1).props().label).to.contain('Groups in Fun Department');
    expect(mounted.find('.list-group').first().find(RadioButton).at(1).find(Checkbox)).to.have.length(5);
  });

  it('displays undergraduate group subsets', () => {
    const props = {
      isGod: false,
      departments: {
        MU: { name: 'Turnip Department', faculty: 'Vegetable' },
      },
      formData: {
        audience: { department: { groups: { undergraduates: undefined } } }
      },
      audienceDidUpdate: () => {},
    };

    const mounted = enzyme.mount(<AudiencePicker {...props} />,  { context });
    expect(mounted.find('.list-group').first(1).find(RadioButton).find({value: 'Dept:UndergradStudents:All'})).to.have.length(1);
    expect(mounted.find('.list-group').first(1).find(RadioButton).find({value: 'year'})).to.have.length(1);
    expect(mounted.find('.list-group').first(1).find(RadioButton).find({value: 'Dept:UndergradStudents:First'})).to.have.length(0);

    mounted.setState({ audience: { department: { groups: { undergraduates: { year: { 'Dept:UndergradStudents:First': undefined } } } } } });

    expect(mounted.find('.list-group').first(1).find(RadioButton)
      .find({ value: 'Dept:UndergradStudents:First' })).to.have.length(1);
  })

});
