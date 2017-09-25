/* eslint-env browser */
import ListTile from './ListTile';

export default class MailTile extends ListTile {
  static overridesOnClick() {
    return true;
  }

  onClick() {
    const { content } = this.props;
    const preferences = this.props.preferences || {};
    const externalapp = preferences.externalapp || 'webmail';
    if (externalapp !== 'webmail' &&
      'MyWarwickNative' in window &&
      'openMailApp' in window.MyWarwickNative
    ) {
      window.MyWarwickNative.openMailApp(externalapp);
    } else if (content.href) {
      if (window.navigator.userAgent.indexOf('MyWarwick/') >= 0) {
        window.location = content.href;
      } else {
        window.open(content.href);
      }
    }
  }
}
