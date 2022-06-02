package xyz.qinfengge.douyinapi.config;

import org.springframework.context.annotation.Configuration;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.service.VideoService;

import java.io.File;

/**
 * @author lizhiao
 * @version 1.0
 * @date 2022/5/22 17:14
 */
@Configuration
public class fileToDB {

    private VideoService videoService = (VideoService) ApplicationContextUtil.getBean("videoService");

    public void readFile(){
        String path = "J:/dy";
        String ip = "http://v.gggg.plus/douyin/";
        File file = new File(path);
        String [] filelist = file.list();

        for (int i=0;i< filelist.length;i++){
            Video video = new Video();
            video.setId(i+1);
            String name = filelist[i].replace("#", "")
                    .replace("C__","")
                    .replace("\"","");
            video.setName(name);
            video.setUrl(ip + name);
            System.out.println(video);
            videoService.save(video);
        }
    }
}
