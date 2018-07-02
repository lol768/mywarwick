import * as React from 'react';
import DoNotDisturbView from 'components/views/settings/DoNotDisturbView';

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
  })

});