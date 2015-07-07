define([], function () {
  'use strict';

  var Stream = function(options) {
    this.url = options.url || ("wss://" + window.location.hostname + (location.port ? ':' + location.port : '') + options.path);
  };

  Stream.prototype = {
    connect : function () {
      var self = this;
      var ws = new WebSocket(this.url);
      this.connected = false;
      ws.onopen = function () {
        this.connected = true;
        //console.log('WS Sending request');
        ws.send('{"message":"Client message from main.js"}');
      };
      ws.onclose = function() {
        this.connected = false;
        console.log("WS Closed");
      };
      ws.onmessage = function (message) {
        if (self.onmessage) self.onmessage(message);
        //console.log('WS Got a message from the server:', JSON.parse(message.data));
      };
    }
  };

  return Stream;

});