#! /usr/bin/env node

const fetch = require('node-fetch');

const CONFIG = require('./config.json');

const pushUrl = CONFIG.pushUrl;
const recipient = CONFIG.recipient;
const interval = CONFIG.interval * 1000;
const pushNotifications = CONFIG.pushNotifications;
const pushActivities = CONFIG.pushActivities;

const activityProviders = Object.keys(CONFIG.activityProviders);
const notificationProviders = Object.keys(CONFIG.notificationProviders);

const basicAuthHash = 'Basic ' + new Buffer(CONFIG.username + ':' + CONFIG.password).toString('base64');

function getRandom(arr) {
  const MIN = 0;
  const MAX = arr.length - 1;
  let i = Math.floor(Math.random() * (MAX - MIN + 1) + MIN);
  return arr[i];
}

function buildJsonBody(contentArr) {

  let data = getRandom(contentArr);

  return {
    type: "api-testing",
    title: data.title,
    text: data.text,
    tags: [],
    replace: {},
    recipients: {
      users: [
        recipient
      ],
      groups: []
    }
  }
}

function notificationsPusher() {
  let providerName = getRandom(notificationProviders);
  let url = `${pushUrl}/api/streams/${providerName}/notifications`;
  let contentArr = CONFIG.notificationProviders[providerName];

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': basicAuthHash
    },
    body: JSON.stringify(buildJsonBody(contentArr))
  })
    .then(res =>
      res.json()
    ).then(json =>
    console.log(json)
  ).catch(err =>
    console.log(err)
  );
}

function activitiesPusher() {
  let providerName = getRandom(activityProviders);
  let url = `${pushUrl}/api/streams/${providerName}/activities`;
  let contentArr = CONFIG.activityProviders[providerName];

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': basicAuthHash
    },
    body: JSON.stringify(buildJsonBody(contentArr))
  })
    .then(res =>
      res.json()
    ).then(json =>
    console.log(json)
  ).catch(err =>
    console.log(err)
  );
}

if (pushNotifications)
  setInterval(notificationsPusher, interval);

if (pushActivities)
  setInterval(activitiesPusher, interval);
