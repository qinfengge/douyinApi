package xyz.qinfengge.douyinapi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.SneakyThrows;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.config.SystemConfig;
import xyz.qinfengge.douyinapi.dto.VideoDto;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.properties.FileProperties;
import xyz.qinfengge.douyinapi.service.VideoService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private SystemConfig systemConfig;

    @Async("threadPoolTaskExecutor")
    public void singleVideoInsert() {
        File videoFile = new File(fileProperties.getVideoDir());
        File[] videoFiles = videoFile.listFiles();
        assert videoFiles != null;

        List<Video> videoList = new ArrayList<>();

        // 遍历视频文件
        for (File file : videoFiles) {
            videoList.add(preProcess(file).getVideo());
            if (fileProperties.getGenerateThumbnail()){
                generateThumbnail(file, preProcess(file).getFileName());
            }
        }

        videoService.saveBatch(videoList);
    }


    /**
     * 预处理，会分离文件名中的标签
     * @param file 文件
     * @return VideoDto对象
     */
    private VideoDto preProcess(File file){
        String[] parts = file.getName().split("_");
        List<String> tags = new ArrayList<>();
        List<String> fileNameParts = new ArrayList<>();
        for (String part : parts) {
            int start = part.indexOf("#");
            int end = part.lastIndexOf("#");
            if (start != -1 && start != end) {
                // 多个标签的情况
                String tag = part.substring(start, end + 1);
                tags.add(tag);
                part = part.replace(tag, "");
            } else if (start != -1) {
                // 只有一个标签的情况
                String tag = part.substring(start);
                tags.add(tag);
                part = part.replace(tag, "");
            }
            String namePart = part.replaceAll("#.*?#", "");
            fileNameParts.add(namePart);
        }

        String fileName;
        if (systemConfig.getIsRename()){
            List<String> collect = fileNameParts.stream().filter(v -> !v.isEmpty()).collect(Collectors.toList());

            if (collect.size() > 3){
                collect = collect.subList(1, collect.size() - 1);
            }

            fileName = String.join(" ", collect);

            // 如果文件名只有标签，则设置为标签
            if (ObjectUtil.isEmpty(fileName)){
                fileName = String.join(" ", tags);
            }

            // 去除文件名多余的后缀
            fileName = FileUtil.getPrefix(fileName);

            // 重命名视频
            FileUtil.rename(file, fileName + "." + FileUtil.getSuffix(file), true);
        }else {
            fileName = FileUtil.getPrefix(file);
        }

        return videoBuilder(fileName, tags, file);
    }


    private VideoDto videoBuilder(String fileName, List<String> tags, File file){
        System.out.println("文件名:" + fileName);
        System.out.println("标签:" + tags);

        Video video = new Video();
        video.setName(fileName);
        video.setUrl(systemConfig.getSiteUrl() + fileProperties.getVideoDir() + fileName + "." + FileUtil.getSuffix(file));

        if (fileProperties.getGenerateThumbnail()){
            video.setThumbnail(systemConfig.getSiteUrl() + fileProperties.getThumbnailDir() + fileName + ".jpg");
        }

        video.setTags(tags);

        VideoDto dto = new VideoDto();
        dto.setFileName(fileName);
        dto.setVideo(video);

        return dto;
    }

    @SneakyThrows
    private void generateThumbnail(File file, String fileName){
        File thumbFile = new File(fileProperties.getThumbnailDir());
        String fileAbsolutePath = file.getAbsolutePath();
        // 如果不存在缩略图文件夹，则创建
        if (!thumbFile.exists()){
            FileUtil.mkdir(fileProperties.getThumbnailDir());
        }
        // 拼接重命名后的缩略图文件路径

        String newName = fileProperties.getThumbnailDir() + "/" + fileName + ".jpg";

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
