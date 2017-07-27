import SmsNotificationsView from 'components/views/settings/SmsNotificationsView';
import * as enzyme from 'enzyme';
import * as React from 'react';
import * as smsNotifications from 'state/sms-notifications';

describe('SmsNotificationsView', () => {

  const commonProps = {
    dispatch: () => {},
    isOnline: true,
    fetching: false,
    fetched: true,
    failed: false,
  };

  const stub = sinon.stub(smsNotifications, 'persist');

  after(() => {
    stub.reset();
  });

  it('prompts for a phone number when enabling if none defined', () => {
    const props = {
      ...commonProps,
      enabled: false,
      smsNumber: null,
    };

    const component = enzyme.shallow(<SmsNotificationsView.WrappedComponent {...props}  />);
    component.instance().onSwitchChange();
    expect(component.state('editing')).to.equal(true);
    expect(component.state('fromEmpty')).to.equal(true);
    expect(component.find('[id="Settings:SMSNumber-input"]')).to.have.length(1);
  });

  it('saves enabling if phone number defined', () => {
    const props = {
      ...commonProps,
      enabled: false,
      smsNumber: "1234",
    };

    const component = enzyme.shallow(<SmsNotificationsView.WrappedComponent {...props}  />);
    component.instance().onSwitchChange();
    expect(stub.calledWith(true, "1234")).to.equal(true);
  });

  it('submit from empty', () => {
    const props = {
      ...commonProps,
      enabled: false,
      smsNumber: null,
    };

    stub.returns(Promise.resolve({ json: () => ({ success: true }) }));
    const component = enzyme.shallow(<SmsNotificationsView.WrappedComponent {...props}  />);
    component.instance().onSwitchChange();
    component.instance().phoneNumberInput = { value: '1234' };
    component.instance().onEditSubmit();
    expect(stub.calledWith(true, "1234")).to.equal(true);
  });

  it('submit from nonempty', () => {
    const props = {
      ...commonProps,
      enabled: false,
      smsNumber: '1234',
    };

    stub.returns(Promise.resolve({ json: () => ({ success: true }) }));
    const component = enzyme.shallow(<SmsNotificationsView.WrappedComponent {...props}  />);
    component.instance().onEdit();
    expect(component.state('editing')).to.equal(true);
    expect(component.state('fromEmpty')).to.equal(false);
    expect(component.find('[id="Settings:SMSNumber-input"]')).to.have.length(1);
    component.instance().phoneNumberInput = { value: '2345' };
    component.instance().onEditSubmit();
    expect(stub.calledWith(false, "2345")).to.equal(true);
  });

  it('handle json success', () => {
    const props = {
      ...commonProps,
      enabled: false,
      smsNumber: '1234',
    };

    const component = enzyme.shallow(<SmsNotificationsView.WrappedComponent {...props}  />);
    component.setState({
      editing: true,
      submitting: true,
    });
    component.instance().phoneNumberInput = { value: '2345' };
    component.instance().handleJson({
      success: true,
    });
    expect(component.state('editing')).to.equal(false);
    expect(component.state('fromEmpty')).to.equal(false);
    expect(component.state('submitting')).to.equal(false);
    expect(component.state('smsNumber')).to.equal('2345');
  });

  it('handle json corrected error', () => {
    const props = {
      ...commonProps,
      enabled: false,
      smsNumber: '1234',
    };

    const component = enzyme.shallow(<SmsNotificationsView.WrappedComponent {...props}  />);
    component.setState({
      editing: true,
      submitting: true,
    });
    component.instance().phoneNumberInput = { value: '' };
    component.instance().handleJson({
      success: false,
      errors: [{ message: 'Empty' }],
    });
    expect(component.state('editing')).to.equal(true);
    expect(component.state('submitting')).to.equal(false);
    expect(component.state('smsNumber')).to.equal('1234');
    expect(component.state('submitError')).to.equal('Empty');
    expect(component.find('.has-error')).to.have.length(1);

    component.setState({
      submitting: true,
    });
    component.instance().phoneNumberInput = { value: '2345' };
    component.instance().handleJson({
      success: true,
      errors: [],
    });
    expect(component.state('editing')).to.equal(false);
    expect(component.state('submitting')).to.equal(false);
    expect(component.state('smsNumber')).to.equal('2345');
    expect(component.state('submitError')).to.equal(undefined);
    expect(component.find('.has-error')).to.have.length(0);
  });

});