import { isEmbedded } from './embedHelper';

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

    if (isDesktop || isEmbedded() || true) {
      return $('.main-content').width() + margins;
    }

    return deviceWidth + margins;
  }
}
