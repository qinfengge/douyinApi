package xyz.qinfengge.douyinapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.result.Result;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
*
*/
public interface VideoService extends IService<Video> {

    Result<Object> init(String isRename) throws IOException;
}
