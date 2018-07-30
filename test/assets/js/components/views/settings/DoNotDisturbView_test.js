import * as React from 'react';
import DoNotDisturbView from 'components/views/settings/DoNotDisturbView';
import { shallow } from 'enzyme';

describe('DoNotDisturbView', () => {

  const sandbox = sinon.sandbox.create();

  afterEach(function () {
    sandbox.restore();
  });

  it('custom proptype validator validates correct 24 hour clock time', () => {
    sandbox.stub(console, 'error');
    const dnd =
      <DoNotDisturbView.WrappedComponent
        dispatch={() => {}}
        isOnline={true}
        enabled={true}
        start={{hr: 21, min: 34}}
        end={{hr: 7, min: 52}}
      />;

    sinon.assert.notCalled(console.error);
  });

  it('custom proptype validator errors on incorrect hours', () => {
    sandbox.stub(console, 'error');
    const dnd =
      <DoNotDisturbView.WrappedComponent
        dispatch={() => {}}
        isOnline={true}
        enabled={true}
        start={{hr: 29, min: 34}}
        end={{hr: 7, min: 52}}
      />;

    sinon.assert.calledOnce(console.error);
  });

  it('custom proptype validator errors on incorrect minutes', () => {
    sandbox.stub(console, 'error');
    const dnd =
      <DoNotDisturbView.WrappedComponent
        dispatch={() => {}}
        isOnline={true}
        enabled={true}
        start={{hr: 21, min: 34}}
        end={{hr: 7, min: 61}}
      />;

    sinon.assert.calledOnce(console.error);
  });

  it('custom proptype validator errors on negative hours and minutes out of range', () => {
    sandbox.stub(console, 'error');
    const dnd =
      <DoNotDisturbView.WrappedComponent
        dispatch={() => {}}
        isOnline={true}
        enabled={true}
        start={{hr: -1, min: 34}}
        end={{hr: 7, min: 60}}
      />;

    sinon.assert.calledTwice(console.error);
  });

  it('calculates dnd period difference on 24 hour clock', () => {
    const wrapper = shallow(
      <DoNotDisturbView.WrappedComponent
        dispatch={() => {}}
        isOnline={true}
        enabled={true}
        start={{ hr: 1, min: 0 }}
        end={{ hr: 7, min: 0 }}
      />
    );
    expect(wrapper.instance().dndPeriodHrs()).to.equal(6);

    const setTime = (start, end) =>
      wrapper.setProps({
        start: { hr: start, min: 0 },
        end: { hr: end, min: 0 }
      });

    setTime(23, 7);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(8);

    setTime(12, 9);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(21);

    setTime(18, 0);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(6);

    setTime(11, 9);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(22);

    setTime(0, 9);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(9);

    setTime(11, 0);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(13);

    setTime(18, 7);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(13);

    setTime(11, 13);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(2);

    setTime(11, 12);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(1);

    setTime(12, 13);
    expect(wrapper.instance().dndPeriodHrs()).to.equal(1);

  });

});