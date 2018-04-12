import ActivityMutingView, * as constants from 'components/views/ActivityMutingView';
import * as React from 'react';
import * as enzyme from 'enzyme';

describe('ActivityMutingView', () => {

  const commonProps = {
    id: '123',
    provider: 'providerIdVal',
    activityType: 'activityTypeVal',
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
    expect(wrapper.state().scope).to.equal(null);
  });

  it('toggles state', () => {
    const props = {
      ...commonProps,
      onMutingDismiss: () => {},
      onMutingSave: () => {},
    };
    const wrapper = enzyme.shallow(<ActivityMutingView {...props} />);
    wrapper.instance().handleScopeChange(constants.PROVIDER_SCOPE);
    expect(wrapper.state().scope).to.equal(constants.PROVIDER_SCOPE);
  });

  /**
   * Asserts that a given state results in the expected
   * argument being called back out to onMutingSave.
   */
  const assertPersistence = (state, expectedArgument) => {
    const props = {
      ...commonProps,
      onMutingDismiss: sinon.spy(),
      onMutingSave: sinon.spy(),
    };
    const wrapper = enzyme.shallow(<ActivityMutingView {...props} />);
    wrapper.setState(state);
    wrapper.instance().saveMuting({ type: 'click', currentTarget: { blur: () => {} } });
    sinon.assert.calledOnce(props.onMutingSave);
    sinon.assert.calledWith(props.onMutingSave, expectedArgument);
  };

  it('persists a provider-only mute correctly', () => {
    const state = { duration: '1hour', scope: constants.PROVIDER_SCOPE };
    const expectedArgument = {
      activityType: null,
      providerId: 'providerIdVal',
      duration: '1hour',
    };
    assertPersistence(state, expectedArgument);
  });

  it('persists an activity type mute correctly', () => {
    const state = { duration: '1hour', scope: constants.TYPE_SCOPE };
    const expectedArgument = {
      activityType: 'activityTypeVal',
      providerId: 'providerIdVal', // still scoped to provider
      duration: '1hour',
    };
    assertPersistence(state, expectedArgument);
  });

});