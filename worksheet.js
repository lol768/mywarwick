var Rx = require('rx');


const Dispatcher = new Rx.Subject();

const peopleResults = new Rx.Subject();
peopleResults.map((x) => '[Mappy bird:'+x+']').subscribe(Dispatcher);

Dispatcher.forEach((d) => {
  console.log("Got a real thingy happening here: " + d);
})

Dispatcher.onNext("Direct");
peopleResults.onNext("PeopleResult");

//promise.reject(new Error("OH NOES"))