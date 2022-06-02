package xyz.qinfengge.douyinapi.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.result.Result;
import xyz.qinfengge.douyinapi.service.VideoService;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @PostMapping("init")
    public Result init(){
        //本地已处理过的视频路径
        String path = "J:/douyinDown/TikTokDownload-main/Download/like/like";
        //上传到的服务器路径 用于拼接视频URL
        String ip = "https://xxx.com/like/";
        File file = new File(path);
        String [] filelist = file.list();

        for (int i=0;i< filelist.length;i++){
            Video video = new Video();
            video.setId(i+1);
//            String name = filelist[i].replaceAll("#", "")
//                    .replace("C__","")
//                    .replaceAll("\"","")
//                    .replaceAll("//////“","")
//                    .replaceAll("【","")
//                    .replaceAll("】","");
            video.setName(filelist[i]);
            video.setUrl(ip + filelist[i]);
            System.out.println(video);
            videoService.save(video);
        }
        return Result.ok("init成功！");
    }


    /**
     * 随机一条视频
     * @return
     */
    @GetMapping("random")
    public Result random(){
        //获取表中数据总数
        long count = videoService.count();
        int random = RandomUtil.randomInt(1,(int)count);
        Video video = videoService.getById(random);
        return Result.ok(video);
    }

    /**
     * 随机5条视频
     * @return
     */
    @GetMapping("randomList")
    public Result randomList(){
        int count = (int) videoService.count();
        int [] all = new int[count];
        for (int i=0;i<count;i++){
            all[i] = i+1;
        }
        //把int数组转为collection集合
        List<Integer> collect = Arrays.stream(all).boxed().collect(Collectors.toList());
        Set<Integer> integers = RandomUtil.randomEleSet(collect, 5);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", integers);
        List<Video> list = videoService.list(queryWrapper);
        return Result.ok(list);
    }


    /**
     * 根据前端传值排除已看过的视频
     * 返回5条数据
     * @param playedIds
     * @return
     */
    @PostMapping("exRandom")
    public Result exRandom(@RequestBody Integer[] playedIds){
        int count = (int) videoService.count();

        Set<Integer> notPlayList = new HashSet<Integer>();

        for (int i=0;i<count;i++){
            notPlayList.add(i+1);
        }

        Set<Integer> hasPlayList = Stream.of(playedIds).collect(Collectors.toSet());

        //把全部的视频ID和已经播放过的视频ID都转为SET集合
        //使用removeAll方法求差集
        notPlayList.removeAll(hasPlayList);
        System.out.println(notPlayList);
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
