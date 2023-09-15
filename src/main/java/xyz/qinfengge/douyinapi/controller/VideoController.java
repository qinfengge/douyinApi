package xyz.qinfengge.douyinapi.controller;

import cn.hutool.bloomfilter.BitSetBloomFilter;
import cn.hutool.bloomfilter.BloomFilterUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.*;
import xyz.qinfengge.douyinapi.config.IdsConfig;
import xyz.qinfengge.douyinapi.config.SystemConfig;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.result.Result;
import xyz.qinfengge.douyinapi.service.VideoService;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author lizhiao
 * @version 1.0
 * @date 2022/5/22 17:55
 */
@RestController
@RequestMapping("douyin")
@CrossOrigin
public class VideoController {

    @Resource
    private VideoService videoService;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private IdsConfig idsConfig;

    private final Integer size = 5;

    @PostMapping("init")
    public Result<Object> init() throws IOException {
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
    public Result<Object> random(){
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
    public Result<Object> randomList(){
        List<Integer> all = idsConfig.getAllIds();
        //把int数组转为collection集合
        //List<Integer> collect = Arrays.stream(all).boxed().collect(Collectors.toList());
        Set<Integer> integers = RandomUtil.randomEleSet(all, 5);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", integers);
        List<Video> list = videoService.list(queryWrapper);
        return Result.ok(list);
    }



    /**
     * 根据前端传值排除已看过的视频
     * 返回5条数据
     * @param playedIds 已看过的视频ID
     * @return 5条未看过的视频
     */
    @PostMapping("exRandom")
    public Result<Object> exRandom(@RequestBody Integer[] playedIds){
        List<Integer> allIds = idsConfig.getAllIds();

        //创建布隆过滤器并循环添加已看过的视频ID
        BitSetBloomFilter bitSet = BloomFilterUtil.createBitSet(allIds.size(), allIds.size(), 10);
        Arrays.stream(playedIds).collect(Collectors.toSet())
                .stream().iterator().forEachRemaining(v->bitSet.add(v.toString()));

        List<Integer> resultIds = new ArrayList<>(size);

        //获取指定大小且未观看过的视频ID
        while (allIds.size() - playedIds.length >= size && size - resultIds.size() > 0){
            RandomUtil.randomEleSet(allIds, size).forEach(v->{
                if (!bitSet.contains(v.toString()) && resultIds.size() < size){
                    resultIds.add(v);
                    bitSet.add(v.toString());
                }
            });
        }

        if (allIds.size() - playedIds.length < size) {
            //如果剩余视频数量小于等于size，直接返回剩余视频
            resultIds.addAll(allIds.stream().filter(v -> !bitSet.contains(v.toString())).collect(Collectors.toList()));
        }


        if (!resultIds.isEmpty()){
            LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Video::getId, resultIds);
            return Result.ok(videoService.list(wrapper));
        }else {
            return Result.fail("没有更多视频了！");
        }
    }

    /**
     * 根据前端传值获取上一个视频
     * @param id
     * @return
     */
    @PostMapping("findById/{id}")
    public Result<Object> findById(@PathVariable Integer id){
        return Result.ok(videoService.getById(id));
    }
}
