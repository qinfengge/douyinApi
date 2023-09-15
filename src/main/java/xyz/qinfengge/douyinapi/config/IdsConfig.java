package xyz.qinfengge.douyinapi.config;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.service.VideoService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lza
 * @date 2023/09/13-16:22
 **/

@Component
public class IdsConfig {

    @Resource
    private VideoService videoService;

    @PostConstruct
    @Cacheable(value = "video", key = "'all'")
    public List<Integer> getAllIds() {
        List<Video> videos = videoService.list();
        List<Integer> all = new ArrayList<>();
        for (Video video : videos) {
            all.add(video.getId());
        }
        return all;
    }
}
