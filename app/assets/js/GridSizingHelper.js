import { isEmbedded } from './embedHelper';
import _ from 'lodash-es';
import $ from 'jquery';

/**
 * Common class to hold grid size
 * related logic.
 */
export class GridSizingHelper {
  static getGridLayoutWidth(props, marginList) {
    const { isDesktop, deviceWidth } = props;

    const margins = _.sum(marginList);

    // FIXME isDesktop is always false

    // TODO (important!) Check that this doesn't inadvertently break NEWSTART-594

    /* eslint-disable */
    if (isDesktop || isEmbedded() || true) {
      return $('.main-content').width() + margins;
    }
    /* eslint-enable */

    return deviceWidth + margins;
  }
}
