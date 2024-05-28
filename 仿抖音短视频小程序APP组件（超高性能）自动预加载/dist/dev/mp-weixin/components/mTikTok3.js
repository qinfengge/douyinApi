"use strict";
const common_vendor = require("../common/vendor.js");
const _sfc_main = /* @__PURE__ */ common_vendor.defineComponent({
  __name: "mTikTok3",
  props: {
    /**
     * 视频列表
     */
    videoList: {
      type: Array,
      default: () => []
    },
    /**
     * 是否循环播放一个视频
     */
    loop: {
      type: Boolean,
      default: true
    },
    /**
     * 显示原生控制栏
     */
    controls: {
      type: Boolean,
      default: false
    },
    /**
     * 是否自动播放
     */
    autoplay: {
      type: Boolean,
      default: true
    },
    /**
     * 是否自动滚动播放
     */
    autoChange: {
      type: Boolean,
      default: false
    },
    /**
     * 滚动加载阈值（即播放到剩余多少个之后触发加载更多
     */
    loadMoreOffsetCount: {
      type: Number,
      default: 2
    },
    /**
     * 视频自动自适应平铺模式
     * 竖屏cover，横屏自适应
     */
    autoObjectFit: {
      type: Boolean,
      default: true
    }
  },
  emits: [
    "play",
    "error",
    "loadMore",
    "change",
    "controlstoggle",
    "click",
    "ended"
  ],
  setup(__props, { expose, emit: emits }) {
    const props = __props;
    const _this = common_vendor.getCurrentInstance();
    const state = common_vendor.reactive({
      originList: [],
      // 源数据
      displaySwiperList: [],
      // swiper需要的数据
      displayIndex: 0,
      // 用于显示swiper的真正的下标数值只有：0，1，2。
      originIndex: 0,
      // 记录源数据的下标
      current: 0,
      oid: 0,
      videoContexts: [],
      isFirstLoad: true,
      isPlaying: false,
      audioContexts: []
    });
    const initVideoContexts = () => {
      state.videoContexts = [
        common_vendor.index.createVideoContext("video__0", _this),
        common_vendor.index.createVideoContext("video__1", _this),
        common_vendor.index.createVideoContext("video__2", _this)
      ];
    };
    const onPlay = (e) => {
      state.isPlaying = true;
      initFirstLoad();
      emits("play", e);
    };
    function handleClick(e) {
      emits("click", e);
    }
    function ended() {
      if (props.autoChange) {
        if (state.displayIndex < 2) {
          state.current = state.displayIndex + 1;
        } else {
          state.current = 0;
        }
      }
      emits("ended");
    }
    function initSwiperData(originIndex = state.originIndex) {
      const originListLength = state.originList.length;
      const displayList = [];
      displayList[state.displayIndex] = state.originList[originIndex];
      displayList[state.displayIndex - 1 == -1 ? 2 : state.displayIndex - 1] = state.originList[originIndex - 1 == -1 ? originListLength - 1 : originIndex - 1];
      displayList[state.displayIndex + 1 == 3 ? 0 : state.displayIndex + 1] = state.originList[originIndex + 1 == originListLength ? 0 : originIndex + 1];
      state.displaySwiperList = displayList;
      if (state.oid >= state.originList.length) {
        state.oid = 0;
      }
      if (state.oid < 0) {
        state.oid = state.originList.length - 1;
      }
      state.videoContexts.map((item) => item == null ? void 0 : item.pause());
      setTimeout(() => {
        if (props.autoplay) {
          common_vendor.index.createVideoContext(`video__${state.displayIndex}`, _this).pause();
          togglePlay();
        }
      }, 500);
      emits("change", {
        index: originIndex,
        detail: state.originList[originIndex]
      });
      var pCount = state.originList.length - props.loadMoreOffsetCount;
      if (originIndex == pCount) {
        emits("loadMore");
      }
    }
    function swiperChange(event) {
      const { current } = event.detail;
      state.isFirstLoad = false;
      const originListLength = state.originList.length;
      if (state.displayIndex - current == 2 || state.displayIndex - current == -1) {
        state.originIndex = state.originIndex + 1 == originListLength ? 0 : state.originIndex + 1;
        state.displayIndex = state.displayIndex + 1 == 3 ? 0 : state.displayIndex + 1;
        state.oid = state.originIndex - 1;
        initSwiperData(state.originIndex);
      } else if (state.displayIndex - current == -2 || state.displayIndex - current == 1) {
        state.originIndex = state.originIndex - 1 == -1 ? originListLength - 1 : state.originIndex - 1;
        state.displayIndex = state.displayIndex - 1 == -1 ? 2 : state.displayIndex - 1;
        state.oid = state.originIndex + 1;
        initSwiperData(state.originIndex);
      }
      if (state.audioContexts[0]) {
        state.audioContexts[0].destroy();
      }
      if (state.originList[state.originIndex].audio && state.originList[state.originIndex].images) {
        const innerAudioContext = common_vendor.index.createInnerAudioContext();
        state.audioContexts[0] = innerAudioContext;
        innerAudioContext.autoplay = true;
        innerAudioContext.volume = 0.3;
        innerAudioContext.loop = true;
        innerAudioContext.src = state.originList[state.originIndex].audio;
        innerAudioContext.onPlay(() => {
          console.log("开始播放:" + state.originList[state.originIndex].audio);
          console.log("当前所在位置:" + current);
        });
        innerAudioContext.onError((res) => {
          console.log(res.errMsg);
          console.log(res.errCode);
        });
      }
    }
    function controlstoggle(e) {
      emits("controlstoggle", e);
    }
    const togglePlay = () => {
      const video = common_vendor.index.createVideoContext(`video__${state.displayIndex}`, _this);
      if (state.isPlaying) {
        video.pause();
        state.isPlaying = false;
      } else {
        video.play();
        state.isPlaying = true;
      }
    };
    const playSeeked = (value) => {
      const video = common_vendor.index.createVideoContext(`video__${state.displayIndex}`, _this);
      video.seek(value);
    };
    common_vendor.watch(
      () => props.videoList,
      () => {
        var _a, _b;
        if ((_a = props.videoList) == null ? void 0 : _a.length) {
          state.originList = props.videoList;
          if (state.isFirstLoad || !((_b = state.videoContexts) == null ? void 0 : _b.length)) {
            initSwiperData();
            initVideoContexts();
          }
        }
      },
      {
        immediate: true
      }
    );
    let loadTimer = null;
    const initFirstLoad = () => {
      if (state.isFirstLoad) {
        loadTimer = setTimeout(() => {
          state.isFirstLoad = false;
          clearTimeout(loadTimer);
        }, 5e3);
      }
    };
    const loadVideoData = ($event, item) => {
      if (item.objectFit) {
        return;
      }
      if (!props.autoObjectFit) {
        return;
      }
      if ($event.detail.width < $event.detail.height) {
        item.objectFit = "cover";
      } else {
        item.objectFit = "contain";
      }
    };
    common_vendor.onUnload(() => {
      clearTimeout(loadTimer);
    });
    expose({
      initSwiperData,
      togglePlay,
      playSeeked
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.f(state.displaySwiperList, (item, index, i0) => {
          return common_vendor.e({
            a: item.url
          }, item.url ? common_vendor.e({
            b: index === 0 || !state.isFirstLoad
          }, index === 0 || !state.isFirstLoad ? {
            c: item.url,
            d: `video__${index}`,
            e: __props.controls,
            f: __props.controls,
            g: item.objectFit,
            h: __props.loop,
            i: common_vendor.o(ended, index),
            j: common_vendor.o(controlstoggle, index),
            k: common_vendor.o(onPlay, index),
            l: common_vendor.o(($event) => emits("error"), index),
            m: common_vendor.o(($event) => loadVideoData($event, item), index)
          } : {}, {
            n: !__props.controls && state.displayIndex === index
          }, !__props.controls && state.displayIndex === index ? {
            o: !state.isPlaying ? 1 : "",
            p: common_vendor.o(togglePlay, index)
          } : {}, {
            q: item.thumbnail && state.displayIndex != index
          }, item.thumbnail && state.displayIndex != index ? {
            r: item.thumbnail
          } : {}, {
            s: "d-" + i0,
            t: common_vendor.r("d", {
              item
            }, i0),
            v: common_vendor.o(handleClick, index)
          }) : {}, {
            w: item.images
          }, item.images ? {
            x: common_vendor.f(item.images, (pic, index2, i1) => {
              return {
                a: pic,
                b: "d-" + i0 + "-" + i1,
                c: common_vendor.r("d", {
                  item
                }, i0 + "-" + i1),
                d: index2
              };
            }),
            y: __props.autoplay
          } : {}, {
            z: index
          });
        }),
        b: common_vendor.o(swiperChange),
        c: state.current
      };
    };
  }
});
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "F:/code/仿抖音短视频小程序APP组件（超高性能）自动预加载/src/components/mTikTok3.vue"]]);
wx.createComponent(Component);
