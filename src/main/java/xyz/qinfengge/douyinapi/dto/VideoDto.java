package xyz.qinfengge.douyinapi.dto;

import lombok.Data;
import xyz.qinfengge.douyinapi.entity.Video;

/**
 * @author lza
 * @date 2023/10/11-16:56
 **/

@Data
public class VideoDto {

    private String fileName;
    private Video video;
}
