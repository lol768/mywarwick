import { mergeNotifications } from 'notifications';

import Immutable from 'immutable';

describe('mergeNotifications', () => {

    let notification = (key, date) => ({key: key, date: date});

    it('adds the first notification', () => {
        let a = notification('a', 1);

        let notifications = mergeNotifications(Immutable.List(), [a]);

        expect(notifications.count()).to.equal(1);
        expect(notifications.first()).to.equal(a);
    });

    it('inserts a newer notification at the start', () => {
        let a = notification('a', 1),
            b = notification('b', 2);

        let notifications = mergeNotifications(Immutable.List([a]), [b]);

        expect(notifications.count()).to.equal(2);
        expect(notifications.first()).to.equal(b);
        expect(notifications.get(1)).to.equal(a);
    });

    it('does not add the same notification twice', () => {
        let a = notification('a', 1);

        let notifications = mergeNotifications(mergeNotifications(Immutable.List(), [a]), [a]);

        expect(notifications.count()).to.equal(1);
    });

    it('uses the newer notification when merging notifications with the same key', () => {
        // Don't expect to use this behaviour, just seeking to define it

        let a = notification('a', 1),
            a2 = notification('a', 2);

        let notifications = mergeNotifications(Immutable.List([a]), [a2]);

        expect(notifications.count()).to.equal(1);
        expect(notifications.first()).to.equal(a2);

        // Check that it's the same regardless of which notification is 'new'

        let notifications2 = mergeNotifications(Immutable.List([a2]), [a]);

        expect(notifications.equals(notifications2)).to.equal(true);
    });

    it('has a defined sort order', () => {
        // Ensure we won't have notifications jiggling around depending on the
        // order received
        let a = notification('a', 1),
            b = notification('b', 1);

        let notifications = mergeNotifications(Immutable.List(), [a, b]);
        let notifications2 = mergeNotifications(Immutable.List(), [b, a]);

        expect(notifications.equals(notifications2)).to.equal(true);
    })

});

