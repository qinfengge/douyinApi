package xyz.qinfengge.douyinapi.util;

import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.properties.FileProperties;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lza
 * @date 2023/10/10-17:06
 **/

@Component
public class ThumbnailUtil {

    @Resource
    private FileProperties fileProperties;

    @SneakyThrows
    public void generateThumbnail() {
        File videoFile = new File(fileProperties.getVideoDir());
        File[] videoFiles = videoFile.listFiles();
        assert videoFiles != null;
        for (File file : videoFiles) {
            String fileAbsolutePath = file.getAbsolutePath();
            String newName = fileProperties.getThumbnailDir() + "/" + doRename(file) + ".jpg";

            FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(fileAbsolutePath);
            grabber.start();
            for (int i = 0; i < fileProperties.getVideoFrame(); i++) {
                grabber.grabImage();
            }

            Frame frame = grabber.grabImage();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage thumbnail = converter.getBufferedImage(frame);
            if (thumbnail != null) {
                ImageIO.write(thumbnail, "jpg", new File(newName));
                System.out.println("缩略图已创建：" + newName);
            }

            grabber.stop();
        }
    }


    private String doRename(File file){
        //使用正则去除文件名中的特殊符号
        //如果文件名中包含emoji，则可能失效
        String name = file.getName();
        String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\]<>/?￥%…（）_+|【】‘；：”“’。，、？\\s]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(name);
        return m.replaceAll("");
    }
}
