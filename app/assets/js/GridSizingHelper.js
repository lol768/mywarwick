import _ from 'lodash-es';
import $ from 'jquery';

/**
 * Common class to hold grid size
 * related logic.
 */
export class GridSizingHelper {
  static getGridLayoutWidth(marginList) {
    const margins = _.sum(marginList);
    return $('.main-content').width() + margins;
  }
}
