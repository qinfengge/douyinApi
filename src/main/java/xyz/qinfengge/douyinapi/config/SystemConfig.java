package xyz.qinfengge.douyinapi.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author lizhiao
 * @date 2022/8/30 14:26
 */
@Component
@Data
public class SystemConfig {

    @Value("${site.url}")
    private String siteUrl;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${file.input.dir}")
    private String fileInputDir;

    @Value("${isRename}")
    private Boolean isRename;
}
