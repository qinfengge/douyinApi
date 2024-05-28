"use strict";
const common_vendor = require("./vendor.js");
function initializeEasterEgg() {
  let keySequence = "";
  const handleKeydown = common_vendor.lodashExports.throttle(function(event) {
    const key = event.key;
    keySequence += key;
    if (keySequence.includes("ArrowUpArrowUpArrowDownArrowDownArrowLeftArrowRightArrowLeftArrowRightBABA")) {
      alert("彩蛋触发！");
      keySequence = "";
    }
  }, 10);
  document.addEventListener("keydown", handleKeydown);
}
exports.initializeEasterEgg = initializeEasterEgg;
