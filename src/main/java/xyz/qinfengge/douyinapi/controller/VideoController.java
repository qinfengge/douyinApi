package xyz.qinfengge.douyinapi.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.qinfengge.douyinapi.config.SystemConfig;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.result.Result;
import xyz.qinfengge.douyinapi.service.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author lizhiao
 * @version 1.0
 * @date 2022/5/22 17:55
 */
@RestController
@RequestMapping("douyin")
@CrossOrigin
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private SystemConfig systemConfig;

    @PostMapping("init")
    public Result init() throws IOException {
        File file = new File(systemConfig.getFileInputDir());
        if (file.exists()){
            return videoService.init(systemConfig.getIsRename());
        }else {
            return Result.fail("输入路径错误！=====" + systemConfig.getFileInputDir());
        }
    }


    /**
     * 随机一条视频
     * @return
     */
    @GetMapping("random")
    public Result random(){
        //获取表中id最大值
        QueryWrapper<Video> wrapper1 = new QueryWrapper<>();
        wrapper1.select("max(id) as id");
        Video max = videoService.getOne(wrapper1);

        QueryWrapper<Video> wrapper2 = new QueryWrapper<>();
        wrapper2.select("min(id) as id");
        Video min = videoService.getOne(wrapper2);
        int random = RandomUtil.randomInt(min.getId(),max.getId());
        Video video = videoService.getById(random);
        return Result.ok(video);
    }

    /**
     * 随机5条视频
     * @return
     */
    @GetMapping("randomList")
    public Result randomList(){
        List<Integer> all = getAllIds();
        //把int数组转为collection集合
        //List<Integer> collect = Arrays.stream(all).boxed().collect(Collectors.toList());
        Set<Integer> integers = RandomUtil.randomEleSet(all, 5);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", integers);
        List<Video> list = videoService.list(queryWrapper);
        return Result.ok(list);
    }

    private List<Integer> getAllIds() {
        List<Video> videos = videoService.list();
        List<Integer> all = new ArrayList<>();
        for (Video video : videos) {
            all.add(video.getId());
        }
        return all;
    }


    /**
     * 根据前端传值排除已看过的视频
     * 返回5条数据
     * @param playedIds
     * @return
     */
    @PostMapping("exRandom")
    public Result exRandom(@RequestBody Integer[] playedIds){
        List<Integer> allIds = getAllIds();

        Set<Integer> notPlayList = new HashSet<>(allIds);


        Set<Integer> hasPlayList = Stream.of(playedIds).collect(Collectors.toSet());

        //把全部的视频ID和已经播放过的视频ID都转为SET集合
        //使用removeAll方法求差集
        notPlayList.removeAll(hasPlayList);
        if (notPlayList.size()>=5){
            Set<Integer> integers = RandomUtil.randomEleSet(notPlayList, 5);
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", integers);

            List<Video> list = videoService.list(queryWrapper);
            return Result.ok(list);
        }else if (notPlayList.size()==0){
            return Result.fail("您已看完全部视频！");
        } else{
            Set<Integer> integers = RandomUtil.randomEleSet(notPlayList, notPlayList.size());
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("id", integers);

            List<Video> list = videoService.list(queryWrapper);
            return Result.ok(list);
        }

    }

    /**
     * 根据前端传值获取上一个视频
     * @param id
     * @return
     */
    @PostMapping("findById/{id}")
    public Result findById(@PathVariable Integer id){
        return Result.ok(videoService.getById(id));
    }
}
