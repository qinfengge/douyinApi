"use strict";
const common_vendor = require("./vendor.js");
const common_config = require("./config.js");
function randomList() {
  return common_vendor.index.request({
    url: `${common_config.baseUrl}/randomList`,
    method: "GET"
  });
}
function exRandom(data) {
  return common_vendor.index.request({
    url: `${common_config.baseUrl}/exRandom`,
    method: "POST",
    data
  });
}
exports.exRandom = exRandom;
exports.randomList = randomList;
