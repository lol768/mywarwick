const FluxStore = require('flux/lib/FluxStore');

const Dispatcher = require('../Dispatcher');

var tab = 'me';

class ApplicationStore extends FluxStore {

    constructor(dispatcher) {
        super(dispatcher);
    }

    getSelectedTab() {
        return tab;
    }

    __onDispatch(action) {
        switch (action.type) {
            case 'select-tab':
                tab = action.tab;
                this.__emitChange();
                break;
            default:
            // no-op
        }
    }


}

export default new ApplicationStore(Dispatcher);