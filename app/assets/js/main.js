const log = require('loglevel');
// only 'warn' otherwise
log.enableAll(false);

const $ = window.jQuery;

const rx = require('rx');
const localforage = require('localforage');
const React = require('react/addons');

const people = require('./tiles/people');
const activitystream = require('./tiles/activitystream');
const TileApp = require('./components/tileapp');
const SocketDataPipe = require('./datapipe/socket');
const FakeDataPipe = require('./datapipe/fake');
const TileStore = require('./stores/tile');

(()=>{

  localforage.config({
    name: 'Start'
  })

})()

$(function(){

  let $tileContainer = $('#tile-container');
  if ($tileContainer.length) {

    let websocket = new SocketDataPipe({
      path: '/websockets/page'
    });

    // Test sending data.
    websocket.send('{"tileId":"3","data":{"query":"Qua"}}');

    // Generate fake data locally
    const data = new FakeDataPipe();
    const store = new TileStore(data);

    // FIXME ask datasource to send us historical data we haven't seen
    // ('last seen' might be different for each tile)
    // In this example we're requesting data for each tile
    // since 1970. If requestData did anything, that is.
    data.requestData({
      tiles: [
        {id:'3', lastItem: new Date(0).getTime()},
        {id:'4', lastItem: new Date(0).getTime()}
      ]
    });

    if (data.fake) {
      //data.fakeNewsItems({tileId: '3', interval: 60000}, () => {
      //  switch (Math.floor(Math.random()*5)) {
      //    case 0: return "Uptown funk";
      //    case 1: return "Good news!";
      //    case 2: return "Nothing at all has happened";
      //    case 3: return "You're a wizard, Harry";
      //    case 4: return "New research in: you're a dumb idiot"
      //  }    log.warn("NOPE")
      //});
      //data.fakeNewsItems({tileId: '4', interval: 134000}, () => "Courses");
    }

    // TODO we won't explicitly list tiles here in production -
    // TileApp will receive some data (from the datapipe?) containing
    // metadata about all the tiles for the user, which it will store
    // in its state and use to generate an array of tile components.
    const app =
      <TileApp store={store}>
        <activitystream.ActivityStreamTile title="News" store={store} key="3" tileId="3" />
        <activitystream.ActivityStreamTile title="T&L" store={store} key="4" tileId="4" />
        <people.PeopleTile title="People" store={store} key="glob" tileId="glob" />
      </TileApp>;

    React.render(app, $tileContainer[0]);

  }
});
