const FluxStore = require('flux/lib/FluxStore');

const Dispatcher = require('../Dispatcher');

// Start the application at the requested URL
var path = window.location.pathname;

var isUpdating = false;
var updateLoaded = 0, updateTotal = 0;

class ApplicationStore extends FluxStore {

    getCurrentPath() {
        return path;
    }

    getUpdateState() {
        return {
            isUpdating: isUpdating,
            loaded: updateLoaded,
            total: updateTotal
        };
    }

    __onDispatch(action) {
        switch (action.type) {
            case 'navigate':
                path = action.path;
                this.__emitChange();
                break;
            case 'app-update-start':
                isUpdating = true;
                this.__emitChange();
                break;
            case 'app-update-progress':
                isUpdating = true;
                updateLoaded = action.loaded;
                updateTotal = action.total;
                this.__emitChange();
                break;
            case 'app-update-ready':
                isUpdating = true;
                updateLoaded = updateTotal;
                this.__emitChange();
                break;
            default:
                // no-op
        }
    }

}

export default new ApplicationStore(Dispatcher);