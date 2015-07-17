var $ = jQuery;
var React = require('react/addons');
var rx = require('rx');

var Stream = require('./websocket').Stream;
var components = require('./components');
var fakedata = require('./fake-datasources');

//window.React = React;

$(function(){
  var $tileContainer = $('#tile-container');
  if ($tileContainer.length) {

    //var updateStream = new Stream({path:"/tilesocket"});

    // Generate fake data locally
    var ds = fakedata.createSource();

    /*<components.ValueTile size="xs" title="A cool number" stream={ds.getTileUpdatesFor("1")} key="1" />
     <components.WeatherTile size="xs" location="Coventry" stream={ds.getTileUpdatesFor("2")} key="2" />*/

    var app =
      <components.TileApp datasource={ds}>
        <components.ActivityStreamTile title="Inbox insite" stream={ds.getTileUpdatesFor("3")} key="3" />
        <components.ActivityStreamTile title="Coursework notifications" stream={ds.getTileUpdatesFor("4")} key="4" />
      </components.TileApp>;

    React.render(app, $tileContainer[0]);

    //updateStream.connect();
  }
});
