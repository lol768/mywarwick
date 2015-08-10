var Rx = require('rx');


var stream = new Rx.Subject();

var promise = Promise.reject(new Error("bad"));

var d = stream.flatMap(
  (v) => {
    console.log('onNext:', v);
    return promise;
  }
).subscribeOnError((err) => console.error("Hey check it:", err));

stream.onNext(3);

//promise.reject(new Error("OH NOES"))