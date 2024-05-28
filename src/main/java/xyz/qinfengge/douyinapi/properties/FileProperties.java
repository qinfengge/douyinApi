package xyz.qinfengge.douyinapi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lza
 * @date 2023/10/10-17:24
 **/
@Component
@Data
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private Boolean generateThumbnail;

    private String videoDir;

    private Integer videoFrame;

}
