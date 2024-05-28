package xyz.qinfengge.douyinapi.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.service.VideoService;
import xyz.qinfengge.douyinapi.mapper.VideoMapper;
import org.springframework.stereotype.Service;


/**
 * @author yasuo
 */
@Service("videoService")
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
        implements VideoService {


}