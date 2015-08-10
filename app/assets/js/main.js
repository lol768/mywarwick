var log = require('loglevel');

// only 'warn' otherwise
log.enableAll();

var $ = jQuery;
var React = require('react/addons');
var rx = require('rx');
var components = require('./components');
var FakeDataPipe = require('./datapipe/fake');

var TileStore = require('./stores/tile');

var localforage = require('localforage');

//window.React = React;

(()=>{

  localforage.config({
    name: 'Start'
  })

})()

$(function(){

  var $tileContainer = $('#tile-container');
  if ($tileContainer.length) {

    // Generate fake data locally
    var ds = new FakeDataPipe();
    var store = new TileStore(ds);

    // FIXME ask datasource to send us historical data we haven't seen
    // ('last seen' might be different for each tile)
    // In this example we're requesting data for each tile
    // since 1970. If requestData did anything, that is.
    ds.requestData({
      tiles: [
        {id:'3', lastItem: new Date(0).getTime()},
        {id:'4', lastItem: new Date(0).getTime()}
      ]
    });

    if (ds.fake) {
      //ds.fakeNewsItems({tileId: '3', interval: 60000}, () => {
      //  switch (Math.floor(Math.random()*5)) {
      //    case 0: return "Uptown funk";
      //    case 1: return "Good news!";
      //    case 2: return "Nothing at all has happened";
      //    case 3: return "You're a wizard, Harry";
      //    case 4: return "New research in: you're a dumb idiot"
      //  }
      //});
      //ds.fakeNewsItems({tileId: '4', interval: 134000}, () => "Courses");
    }

    // TODO we won't explicitly list tiles here in production -
    // TileApp will receive some data (from the datapipe?) containing
    // metadata about all the tiles for the user, which it will store
    // in its state and use to generate an array of tile components.
    var app =
      <components.TileApp store={store}>
        <components.ActivityStreamTile title="News" store={store} key="3" tileId="3" />
        <components.ActivityStreamTile title="T&L" store={store} key="4" tileId="4" />
      </components.TileApp>;

    React.render(app, $tileContainer[0]);

    //updateStream.connect();
  }
});
