define(['bootstrap','jquery','react', 'components','update-stream'], function(bootstrap, $, React, components, Stream){
  console.log("Hello from main.js");


  $(function(){
    var $tileContainer = $('#tile-container');
    if ($tileContainer.length) {
      console.log('Got tile container');

      // initial fake data
      var tiles = [
      ];

      for (var i=1; i<=50; i++) {
        tiles.push({key : i, tileId : i, value : 0});
      }

      var updateStream = new Stream({path:"/tilesocket"});

      // Abstraction for data source, that maybe we can mock up.
      var datasource = {
        tiles: tiles,
        onChange: function(fn) {
          updateStream.onmessage = fn;
        }
      }

      var app = <components.TileApp datasource={datasource} />;
      React.render(app, $tileContainer[0]);

      updateStream.connect();
    }
  })

});