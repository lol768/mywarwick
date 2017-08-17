import ActivityMutingView from 'components/views/ActivityMutingView';
import * as React from 'react';
import * as enzyme from 'enzyme';

describe('ActivityMutingView', () => {

  const commonProps = {
    id: '123',
    provider: 'provider',
    activityType: 'activityType',
    tags: [
      {
        name: 'Tag 1',
        value: 'Tag 1 Value 1',
      },
      {
        name: 'Tag 1',
        value: 'Tag 1 Value 2',
      },
      {
        name: 'Tag 2',
        value: 'Tag 2 Value 1',
      },
    ],
  };

  it('sets up state correctly', () => {
    const props = {
      ...commonProps,
      onMutingDismiss: () => {},
      onMutingSave: () => {},
    };
    const wrapper = enzyme.shallow(<ActivityMutingView {...props} />);
    expect(wrapper.state().formValues.activityType).to.equal(true);
    expect(wrapper.state().formValues.providerId).to.equal(true);
    expect(wrapper.state().formValues['tag-Tag 1-Tag 1 Value 1']).to.equal(true);
    expect(wrapper.state().formValues['tag-Tag 1-Tag 1 Value 2']).to.equal(true);
    expect(wrapper.state().formValues['tag-Tag 2-Tag 2 Value 1']).to.equal(true);
  });

  it('toggles state', () => {
    const props = {
      ...commonProps,
      onMutingDismiss: () => {},
      onMutingSave: () => {},
    };
    const wrapper = enzyme.shallow(<ActivityMutingView {...props} />);
    wrapper.instance().handleCheckboxChange('provider', 'providerId');
    wrapper.instance().handleCheckboxChange('activityType', 'activityType');
    wrapper.instance().handleCheckboxChange('Tag 1 Value 1', 'tag-Tag 1');
    wrapper.instance().handleCheckboxChange('Tag 1 Value 2', 'tag-Tag 1');
    wrapper.instance().handleCheckboxChange('Tag 2 Value 1', 'tag-Tag 2');
    expect(wrapper.state().formValues.activityType).to.equal(false);
    expect(wrapper.state().formValues.providerId).to.equal(false);
    expect(wrapper.state().formValues['tag-Tag 1-Tag 1 Value 1']).to.equal(false);
    expect(wrapper.state().formValues['tag-Tag 1-Tag 1 Value 2']).to.equal(false);
    expect(wrapper.state().formValues['tag-Tag 2-Tag 2 Value 1']).to.equal(false);
  });

  it('creates persistence object correctly', () => {
    const props = {
      ...commonProps,
      onMutingDismiss: sinon.spy(),
      onMutingSave: sinon.spy(),
    };
    const wrapper = enzyme.shallow(<ActivityMutingView {...props} />);
    wrapper.setState({ duration: '1hour' });
    wrapper.instance().saveMuting({ type: 'click', currentTarget: { blur: () => {} } });
    expect(props.onMutingSave.calledOnce).to.equal(true);
    expect(props.onMutingSave.calledWith({
      activityType: 'activityType',
      providerId: 'provider',
      duration: '1hour',
      ['tags[Tag 1]']: 'Tag 1 Value 1',
      ['tags[Tag 1]']: 'Tag 1 Value 2',
      ['tags[Tag 2]']: 'Tag 2 Value 1',
    })).to.equal(true);
  });

});