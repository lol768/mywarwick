#! /usr/bin/env node
'use strict';

var fetch = require('node-fetch');
var async = require('async');

const CONFIG = require('./config.json');

const pushUrl = CONFIG.pushUrl;
const recipient = CONFIG.recipient;
const interval = CONFIG.interval * 1000;
const pushNotifications = CONFIG.pushNotifications;
const pushActivities = CONFIG.pushActivities;

const basicAuthHash = 'Basic ' + new Buffer(CONFIG.username + ':' + CONFIG.password).toString('base64');

function getRandom(min, max) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

function buildNotificationsUrl(providerNum) {
  let providerName = [
    "tabula",
    "library",
    "hear-now"
  ][providerNum];
  return `${pushUrl}/api/streams/${providerName}/notifications`;
}

function buildActivitiesUrl(providerNum) {
  let providerName = [
    "sports-centre",
    "eating"
  ][providerNum];
  return `${pushUrl}/api/streams/${providerName}/activities`;
}

function buildJsonBody(providerNum) {
  let providerName = [
    'tabula',
    'library',
    'hear-now',
    'sports-centre',
    'eating'
  ][providerNum];

  let data = CONFIG.bodyText[providerName][getRandom(0, 2)];

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
  let providerNum = getRandom(0, 2);
  let url = buildNotificationsUrl(providerNum);
  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': basicAuthHash
    },
    body: JSON.stringify(buildJsonBody(providerNum))
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
  let providerNum = getRandom(0, 1);
  fetch(buildActivitiesUrl(providerNum), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': basicAuthHash
    },
    body: JSON.stringify(buildJsonBody(providerNum + 3))
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
