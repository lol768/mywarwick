import { MuteDescription, ActivityMutesView } from 'components/views/settings/ActivityMutesView';
import * as enzyme from 'enzyme';

describe('ActivityMutesView', () => {
  it('renders empty', () => {
    const result = enzyme.shallow(<ActivityMutesView
      dispatch={() => {}}
      activityMutes={[]}
      isOnline={true}
    />);
    result.find('EmptyState').childAt(0).text().should.contain("You haven't muted any alerts");
    result.find('MuteDescription').should.have.length(0);
  })
});

describe('MuteDescription', () => {
  const tags = [{id: 'deptcode', name:'Department', value:'CH', display_value: 'Chemistry'}];
  const activityType = { name: 'coursework-due' };
  const provider = { id: 'tabula' };
  const baseMute = {
    id: "12345",
    usercode: 'cusebr',
    createdAt: '2018-04-12T12:00:00Z',
  };

  it('Displays the old list when tags are present', () => {
    const mute = { ...baseMute, provider, activityType, tags };
    const result = enzyme.shallow(<MuteDescription mute={mute} />);
    const lis = result.find('li');
    lis.should.have.length(3);
    lis.at(0).text().should.equal("'coursework-due' alerts");
    lis.at(1).text().should.equal("tabula alerts");
    lis.at(2).text().should.equal("Chemistry");
  });

  it('Displays provider-only mutes as expected', () => {
    const mute = { ...baseMute, provider };
    const result = enzyme.shallow(<MuteDescription mute={mute} />);
    const lis = result.find('li');
    lis.should.have.length(1);
    lis.at(0).text().should.equal("All tabula alerts");
  });

  it('Handles provider+type mutes as expected', () => {
    const mute = { ...baseMute, activityType, provider };
    const result = enzyme.shallow(<MuteDescription mute={mute} />);
    const lis = result.find('li');
    lis.should.have.length(1);
    lis.at(0).text().should.equal("'coursework-due' alerts from tabula");
  });

  // This case will become uncommon as the new UI will always include
  // the provider in new mutes.
  it('Handles type-only mutes as expected', () => {
    const mute = { ...baseMute, activityType };
    const result = enzyme.shallow(<MuteDescription mute={mute} />);
    const lis = result.find('li');
    lis.should.have.length(1);
    lis.at(0).text().should.equal("'coursework-due' alerts");
  });
});