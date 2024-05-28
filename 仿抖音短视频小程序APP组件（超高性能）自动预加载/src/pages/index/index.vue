<template>
	<div class="video-container">
		<mTikTok ref="mTikTokRef" :video-list="state.videoList" @loadMore="loadMore" @change="change">
			<!-- 此处为用户完全自定义 data 中的数据为当前渲染的数据 -->
			<template v-slot="data">
				<!-- <view class="video-side-right">
					<view class="action-item action-item-user">
						<image class="shop-logo"
							src="https://examples-1251000004.cos.ap-shanghai.myqcloud.com/sample.jpeg?imageMogr2/crop/180x180/gravity/center" />
						<view class="action-btn">
							<text class="iconfont">+</text>
						</view>
						<text class="action-item-text"></text>
					</view>
					<view class="action-item">
						<text class="iconfont icon-star11beifen">❤</text>
						<text class="action-item-text">{{ data.item.id }}</text>
					</view>
					<view class="action-item">
						<text class="iconfont icon-share">☝</text>
						<text class="action-item-text">分享</text>
					</view>
				</view> -->
				<view class="video-bottom-area">
					<view class="shop-name">
						<!-- 如果name长度超过60个字符才显示折叠效果 -->
						<template v-if="data.item.name.length > 20">
							<view v-if="!state.showFull">
								<view>{{ data.item.name.slice(0, 20) }}...</view>
								<!-- 如果需要折叠时才显示"展开"按钮 -->
								<view v-if="data.item.name.length > 20 && !state.showFull">
									<text @click="state.showFull = true" style="font-weight: bold;">展开</text>
								</view>
							</view>
							<view v-else>{{ data.item.name }}</view>
						</template>

						<!-- 否则直接显示完整name -->
						<template v-else>
							{{ data.item.name }}
						</template>
					</view>
					<!-- <view class="shop-card">{{ data.item.desc }}</view> -->
				</view>
			</template>
		</mTikTok>
	</div>
</template>
<script lang="ts" setup>
	import { onBeforeMount, onMounted, reactive, ref } from "vue";
	// 导入组件
	import mTikTok from "@/components/mTikTok3.vue";
	import { exRandom, randomList } from '../../common/api';
	import { initializeEasterEgg } from "../../common/EasterEgg";
	
	
	const mTikTokRef = ref<InstanceType<typeof mTikTok>>();

	const state = reactive({
		videoList: [],
		// 是否展开的标志
		showFull: false
	});

	const loadMore = () => {
		// 触发加载更多
		console.log("加载更多");
		uni.getStorage({
			key: "hasPlayedList",
			success(res) {
				exRandom(res.data).then(response => {
					uploadStorage(response.data.data)
					state.videoList = [...state.videoList, ...response.data.data].slice(-9999);
				})
			}
		})
	};

	const change = (e : any) => {
		mTikTokRef.value?.togglePlay();
		state.showFull = false
		console.log("🚀 ~ file: index.vue:53 ~ change ~ data:", e);
	};

	// 播放第几个
	const playIndex = (index : number) => {
		console.log("触发下标" + index)
		mTikTokRef.value?.initSwiperData(index);
	};

	const uploadStorage = (videoList : any) => {
		let playedIds = []
		uni.getStorage({
			key: "hasPlayedList",
			success: (res) => {
				playedIds = res.data
			}
		})

		let nowIds = videoList.map(video => video.id);
		for (var i = 0; i < nowIds.length; i++) {
			playedIds.push(nowIds[i])
		}

		uni.setStorage({
			key: "hasPlayedList",
			data: Array.from(new Set(playedIds))
		})
		console.log(playedIds)
	}


	onBeforeMount(() => {
		
		const searchHits = getApp().globalData.videoList
		
		// console.log("搜索的值：" + JSON.stringify(searchHits))
		if(searchHits){
			state.videoList = getApp().globalData.videoList
		}else{
			// 调用exRandom方法获取新的视频
			randomList().then(response => {
				console.log(response.data.data)
				// 更新视频列表数据
				state.videoList = response.data.data
				
				uploadStorage(state.videoList)
			});
		}

	});

	onMounted(() => {
		initializeEasterEgg()
		
		const sIndex = getApp().globalData.sIndex
		console.log("sIndex==" + sIndex)
		if(sIndex){
			playIndex(sIndex)
		}
		// 直接播放第3个
		// playIndex(3);
	});
</script>
<style lang="scss">
	.video-layer {
		position: absolute;
		right: 12px;
		bottom: 120px;
		color: #fff;
	}

	.video-bottom-area {
		position: absolute;
		left: 20px;
		bottom: 30px;
		z-index: 999;

		.shop-name {
			color: #fff;
			margin-bottom: 4px;
		}

		.shop-card {
			width: 160px;
			height: 80px;
			background-color: rgba(255, 255, 255, 0.5);
			border-radius: 4px;
		}
	}

	.video-side-right {
		position: absolute;
		right: 12px;
		bottom: 120px;
		color: #fff;
		z-index: 999;

		.action-item {
			position: relative;
			margin-bottom: 20px;
			text-align: center;

			.shop-logo {
				width: 40px;
				height: 40px;
				border-radius: 50%;
				overflow: hidden;
			}

			.iconfont {
				display: block;
				font-size: 28px;
			}

			.action-item-text {
				display: block;
				font-size: 12px;
			}

			.action-btn {
				position: absolute;
				left: 50%;
				transform: translateX(-50%);
				bottom: -8px;
				width: 20px;
				height: 20px;
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
				background-color: #f60;

				.iconfont {
					font-size: 16px;
				}
			}
		}

		.action-item-user {
			margin-bottom: 40px;
		}
	}
</style>