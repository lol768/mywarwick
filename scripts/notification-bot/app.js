#! /usr/bin/env node
/* eslint no-console: 0 */
const fetch = require('node-fetch');

const CONFIG = require('./config.json');

const pushUrl = CONFIG.pushUrl;
const recipient = CONFIG.recipient;
const interval = CONFIG.interval * 1000;
const pushNotifications = CONFIG.pushNotifications;
const pushActivities = CONFIG.pushActivities;

const activityProviders = Object.keys(CONFIG.activityProviders);
const notificationProviders = Object.keys(CONFIG.notificationProviders);

const hash = new Buffer(`${CONFIG.username}:${CONFIG.password}`).toString('base64');
const basicAuthHash = `Basic ${hash}`;

function getRandom(arr) {
  const MIN = 0;
  const MAX = arr.length - 1;
  const i = Math.floor(Math.random() * (MAX - MIN + 1) + MIN);
  return arr[i];
}

function buildJsonBody(contentArr) {
  const data = getRandom(contentArr);

  return {
    type: 'api-testing',
    title: data.title,
    text: data.text,
    tags: [],
    replace: {},
    recipients: {
      users: [
        recipient,
      ],
      groups: [],
    },
  };
}

function notificationsPusher() {
  if (!notificationProviders.length) return;
  const providerName = getRandom(notificationProviders);
  const url = `${pushUrl}/api/streams/${providerName}/alerts`;
  const contentArr = CONFIG.notificationProviders[providerName];

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: basicAuthHash,
    },
    body: JSON.stringify(buildJsonBody(contentArr)),
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
  if (!activityProviders.length) return;
  const providerName = getRandom(activityProviders);
  const url = `${pushUrl}/api/streams/${providerName}/activities`;
  const contentArr = CONFIG.activityProviders[providerName];

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: basicAuthHash,
    },
    body: JSON.stringify(buildJsonBody(contentArr)),
  })
    .then(res =>
      res.json()
    ).then(json =>
    console.log(json)
  ).catch(err =>
    console.log(err)
  );
}

if (pushNotifications) {
  setInterval(notificationsPusher, interval);
}

if (pushActivities) {
  setInterval(activitiesPusher, interval);
}
