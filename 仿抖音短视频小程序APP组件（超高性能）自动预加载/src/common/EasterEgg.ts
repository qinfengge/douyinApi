// EasterEgg.ts

import { throttle } from "lodash";

export function initializeEasterEgg() {
  // 记录用户按下的按键序列
  let keySequence = "";

  // 定义节流函数，每300毫秒处理一次按键事件
  const handleKeydown = throttle(function(event: KeyboardEvent) {
    // 获取按下的按键
    const key = event.key;

    // 将按键追加到按键序列中
    keySequence += key;

    // 如果按键序列匹配到"BABA"
    if (keySequence.includes("ArrowUpArrowUpArrowDownArrowDownArrowLeftArrowRightArrowLeftArrowRightBABA")) {
      // 弹出提示框
      alert("彩蛋触发！");
	  
	  uni.navigateTo({
	  	url: '/pages/search/search'
	  })
	  
      // 清空按键序列，以便下次触发
      keySequence = "";
    }
  }, 10);

  // 监听键盘按下事件，使用节流函数处理事件
  document.addEventListener("keydown", handleKeydown);
}