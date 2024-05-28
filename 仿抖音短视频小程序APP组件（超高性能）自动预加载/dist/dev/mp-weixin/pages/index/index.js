"use strict";
const common_vendor = require("../../common/vendor.js");
const common_api = require("../../common/api.js");
const common_EasterEgg = require("../../common/EasterEgg.js");
require("../../common/config.js");
if (!Math) {
  mTikTok();
}
const mTikTok = () => "../../components/mTikTok3.js";
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "index",
  setup(__props) {
    const mTikTokRef = common_vendor.ref();
    const state = common_vendor.reactive({
      videoList: [],
      // 是否展开的标志
      showFull: false
    });
    const loadMore = () => {
      console.log("加载更多");
      common_vendor.index.getStorage({
        key: "hasPlayedList",
        success(res) {
          common_api.exRandom(res.data).then((response) => {
            uploadStorage(response.data.data);
            state.videoList = [...state.videoList, ...response.data.data].slice(-9999);
          });
        }
      });
    };
    const change = (e) => {
      var _a;
      (_a = mTikTokRef.value) == null ? void 0 : _a.togglePlay();
      state.showFull = false;
      console.log("🚀 ~ file: index.vue:53 ~ change ~ data:", e);
    };
    const uploadStorage = (videoList) => {
      let playedIds = [];
      common_vendor.index.getStorage({
        key: "hasPlayedList",
        success: (res) => {
          playedIds = res.data;
        }
      });
      let nowIds = videoList.map((video) => video.id);
      for (var i = 0; i < nowIds.length; i++) {
        playedIds.push(nowIds[i]);
      }
      common_vendor.index.setStorage({
        key: "hasPlayedList",
        data: Array.from(new Set(playedIds))
      });
      console.log(playedIds);
    };
    common_vendor.onBeforeMount(() => {
      common_api.randomList().then((response) => {
        console.log(response.data.data);
        state.videoList = response.data.data;
        uploadStorage(state.videoList);
      });
    });
    common_vendor.onMounted(() => {
      common_EasterEgg.initializeEasterEgg();
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.w((data, s0, i0) => {
          return common_vendor.e({
            a: data.item.name.length > 20
          }, data.item.name.length > 20 ? common_vendor.e({
            b: !state.showFull
          }, !state.showFull ? common_vendor.e({
            c: common_vendor.t(data.item.name.slice(0, 20)),
            d: data.item.name.length > 20 && !state.showFull
          }, data.item.name.length > 20 && !state.showFull ? {
            e: common_vendor.o(($event) => state.showFull = true)
          } : {}) : {
            f: common_vendor.t(data.item.name)
          }) : {
            g: common_vendor.t(data.item.name)
          }, {
            h: i0,
            i: s0
          });
        }, {
          name: "d",
          path: "a",
          vueId: "6d0151ba-0"
        }),
        b: common_vendor.sr(mTikTokRef, "6d0151ba-0", {
          "k": "mTikTokRef"
        }),
        c: common_vendor.o(loadMore),
        d: common_vendor.o(change),
        e: common_vendor.p({
          ["video-list"]: state.videoList
        })
      };
    };
  }
});
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "F:/code/仿抖音短视频小程序APP组件（超高性能）自动预加载/src/pages/index/index.vue"]]);
wx.createPage(MiniProgramPage);
