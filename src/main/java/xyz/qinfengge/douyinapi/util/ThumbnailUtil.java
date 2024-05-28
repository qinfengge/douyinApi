package xyz.qinfengge.douyinapi.util;


import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.properties.FileProperties;
import xyz.qinfengge.douyinapi.service.VideoService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author lza
 * @date 2023/10/10-17:06
 **/

@Component
public class ThumbnailUtil {

    @Resource
    private FileProperties fileProperties;

    @Resource
    private VideoService videoService;

    @Async("threadPoolTaskExecutor")
    @SneakyThrows
    public void generateThumbnailByFile(File file, String fileName){
        String fileAbsolutePath = file.getAbsolutePath();

        // 拼接重命名后的缩略图文件路径
        String newName = file.getParentFile() + "/" + fileName + ".jpg";

        FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(fileAbsolutePath);
        grabber.start();
        // 跳过设定的帧数
        for (int i = 0; i < fileProperties.getVideoFrame(); i++) {
            grabber.grabImage();
        }

        // 获取指定的视频帧并转为图片写入到文件
        Frame frame = grabber.grabImage();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage thumbnail = converter.getBufferedImage(frame);
        if (thumbnail != null) {
            System.out.println("当前线程：" + Thread.currentThread().getName());
            ImageIO.write(thumbnail, "jpg", new File(newName));
            System.out.println("缩略图已创建：" + newName);
        }

        // 停止抓取
        grabber.stop();

    }
}
