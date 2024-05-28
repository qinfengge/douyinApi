<template>
  <view class="container">
    <view class="search-container">
      <input class="search-input" type="text" v-model="searchQuery" placeholder="Search here..." />
      <button class="search-button" size="mini" @click="search">Search</button>
    </view>
    <view class="results-container">
      <view v-for="(result, index) in searchResults" :key="index" class="result-item" @click="handleToVideo(index)">
        <view class="thumbnail-container">
          <image class="thumbnail" mode="aspectFill" :src="result.thumbnail"></image>
        </view>
        <view class="info-container">
          <text class="result-title">{{ result.name }}</text>
		  <br>
          <text class="result-description">{{ result.tags }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
	import { searchUrl } from '@/common/config';
export default {
  data() {
    return {
      searchQuery: '',
      searchResults: []
    };
  },
  methods: {
    search() {
      // 发起搜索请求
      // 替换为你的MeiliSearch搜索请求
      // 示例：使用fetch API发送GET请求
      fetch(`${searchUrl}?keyword=${this.searchQuery}`)
        .then(response => response.json())
        .then(data => {
          // 处理搜索结果
          console.log(data)
          this.searchResults = data.data.hits;
        })
        .catch(error => {
          console.error('Error searching:', error);
        });
    },
	
	handleToVideo(index){
		console.log("选择了：" + index)
		
		getApp().globalData.videoList = this.searchResults
		getApp().globalData.sIndex = index
		
		// let nowIds = this.searchResults.map(video => video.id);
		// uni.setStorage({
		// 	key: "hasPlayedList",
		// 	data: Array.from(new Set(nowIds))
		// })
		// console.log(nowIds)
		
		uni.navigateTo({
			url: '/pages/index/index'
		})
	}
  },
  
  
};
</script>

<style>
.container {
  padding: 20px;
}

.search-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  margin-right: 10px;
}

.search-button {
  padding: 10px 20px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.results-container {
  display: flex;
  flex-direction: column;
}

.result-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.thumbnail-container {
  width: 175px;
  height: 275px;
  margin-right: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* .thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
} */

.info-container {
  flex: 1;
}

.result-title {
  font-weight: bold;
}

.result-description {
  margin-top: 5px;
  color: #888;
}
</style>
