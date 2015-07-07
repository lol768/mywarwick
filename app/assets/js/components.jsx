define(['react'], function(React) { 'use strict';

  var Tile = React.createClass({
    getInitialState: function () {
      return this.props.data;
    },
    render: function() {
      return <div className="col-md-3 col-sm-4">
        <div className="panel panel-default">
          <div className="panel-heading">Tile {this.props.data.tileId}</div>
          <div className="panel-body">
            <h3>{this.state.value}</h3>

          </div>
        </div>
      </div>;
    }
  });

  var TileApp = React.createClass({
    getInitialState: function() {
      var self = this;
      var ds = this.props.datasource;
      var state = {tiles: ds.tiles};
      ds.onChange(function(msg) {
        msg = JSON.parse(msg.data);
        if (msg.tileId) {
          for (var i = 0; i < state.tiles.length; i++) {
            if (msg.tileId == state.tiles[i].tileId) {
              state.tiles[i].value = msg.value;
            }
          }
          self.setState(state);
        }
      });
      state.tiles[0].value = 1;
      return state;
    },
    render: function () {
      var newTile = function(data, index) {
        return <Tile data={data} key={data.tileId} />;
      };
      return <div className="tiles row">
        {this.state.tiles.map(newTile)}
      </div>;
    },
  });

  return {
    Tile : Tile,
    TileApp : TileApp
  };

});