package xyz.qinfengge.douyinapi.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.config.SystemConfig;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.enums.Type;
import xyz.qinfengge.douyinapi.service.VideoService;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author lza
 * @date 2023/10/12-17:33
 **/


@Component
public class AdvancedUtil {

    @Resource
    private FileUtils fileUtils;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private VideoService videoService;

    @Resource
    private ThumbnailUtil thumbnailUtil;

    @Resource
    private MeilisearchUtil meilisearchUtil;

    @SneakyThrows
    public void walkFileTree(String path){
        AtomicInteger dircount = new AtomicInteger();
        AtomicInteger filecount = new AtomicInteger();

        List<Video> videoList = new ArrayList<>();
        List<Path> pathList = new ArrayList<>();
        List<Path> badFileDir = new ArrayList<>();

        Files.walkFileTree(Paths.get(path),new SimpleFileVisitor<Path>(){
            //进入文件夹触发
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                dircount.incrementAndGet();

                return super.preVisitDirectory(dir, attrs);
            }
            //进入文件触发
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toString().endsWith(".txt")){
                    Map<String, Object> map = fileUtils.rename(file.getFileName().toString(), false);
                    FileUtil.rename(file, map.get("fileName").toString(), false);
                    pathList.add(file.getParent());
                }
                filecount.incrementAndGet();
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                Map<String, Object> map = fileUtils.rename(dir.getFileName().toString(), true);
                System.out.println("文件名:" + map.get("fileName"));
                System.out.println("标签:" + map.get("tags"));
                System.out.println("创建日期:" + map.get("created"));

                List<String> list = FileUtil.listFileNames(dir.toString());

                // 当文件夹中有文件才更新文件夹名，需要上传文件夹内以账号命名的txt文件
                boolean bool = list.stream().anyMatch(r -> r.endsWith(".txt"));

                if (!list.isEmpty() && !bool){
                    System.err.println("需要重命名的文件：" + list);
                    String parent1 = fileUtils.getFileParentName(dir, 1);
                    String parent2 = fileUtils.getFileParentName(dir, 2);

                    Video video = new Video();
                    Boolean containsVideo = fileUtils.containsVideo(dir);
                    video.setName((String) map.get("fileName"));
                    video.setCreated(map.get("created").toString());
                    List<String> tags = JSONUtil.toList(JSONUtil.parseArray(map.get("tags")), String.class);
                    video.setTags(tags);
                    video.setUserName(parent1);
                    video.setType(Type.getTypeCode(parent2));

                    // 当为视频时
                    String shortUrl = parent2 + "/" + parent1 + "/" + map.get("fileName").toString() + "/" + map.get("fileName").toString();

                    // 如果文件夹中有视频或图片
                    if (fileUtils.checkFileDir(dir)){

                        if (containsVideo){
                            Map<String, String> mapSuffix = fileUtils.getDirFileType(dir);

                            if (fileUtils.hasAudio(list)){
                                video.setAudio(systemConfig.getSiteUrl() + shortUrl + ".mp3");
                            }
                            video.setUrl(systemConfig.getSiteUrl() + shortUrl + "." + mapSuffix.get("videoSuffix"));

                            // 如果目录存在图片文件，则设置缩略图选项
                            if (fileUtils.hasThumbnail(dir)){
                                video.setThumbnail(systemConfig.getSiteUrl() + shortUrl + "." + mapSuffix.get("imageSuffix"));
                            }else {
                                // 调用缩略图生成工具，生成缩略图
                                File[] files = FileUtil.file(dir.toString()).listFiles();
                                assert files != null;
                                Optional<File> file = Arrays.stream(files).filter(r -> "mp4".equals(FileUtil.getSuffix(r))).findFirst();
                                thumbnailUtil.generateThumbnailByFile(file.get(), map.get("fileName").toString());
                                video.setThumbnail(systemConfig.getSiteUrl() + shortUrl + ".jpg");
                            }
                        }else {
                            // 当为图集时
                            List<String> images = new ArrayList<>();
                            // 当图集有原声时
                            for (int i = 0; i < list.size(); i++) {
                                Map<String, Object> fileMap = fileUtils.rename(list.get(i), false);
                                shortUrl = parent2 + "/" + parent1 + "/" + map.get("fileName").toString() + "/" + fileMap.get("fileName").toString();
                                if ("mp3".equals(FileUtil.getSuffix(list.get(i)))){
                                    video.setAudio(systemConfig.getSiteUrl() + shortUrl);
                                    images.add(systemConfig.getSiteUrl() + shortUrl);
                                }else {
                                    images.add(systemConfig.getSiteUrl() + shortUrl);
                                }
                            }

                            // 过滤掉集合中的MP3文件
                            List<String> collect = images.stream().filter(r -> !r.endsWith(".mp3")).collect(Collectors.toList());

                            video.setImages(collect);

                            // 设置图集的缩略图为第一张
                            video.setThumbnail(collect.get(0));
                        }
                        System.err.println("video===" + video);
                        videoList.add(video);
                    }else {
                        // 如果文件夹不符合规范
                        FileUtil.del(dir);
                        badFileDir.add(dir);
                    }

                }

                return super.postVisitDirectory(dir, exc);
            }
        });
        videoService.saveBatch(videoList);

        // 初始化MeiliSearch
        Client client = meilisearchUtil.init();
        // 创建索引库 video
        Index index = meilisearchUtil.createIndex(client, "video");
        // 设置搜索字段
        String[] attributes = {"name", "tags", "userName"};
        index.updateSearchableAttributesSettings(attributes);
        // 添加文档到索引库
        String jsonStr = JSONUtil.toJsonStr(videoList);
        meilisearchUtil.addDocument(index, jsonStr);

        System.out.println("文件夹数量："+dircount+"文件数量："+filecount);
        System.err.println("pathList===" + pathList);
        System.err.println("不符合的文件夹个数：" + badFileDir.size());
        System.err.println("不符合的文件夹：" + badFileDir);


        List<Path> collect = pathList.stream().distinct().collect(Collectors.toList());
        // 求差集，去掉全部文件夹中的不合格文件夹
        Collection<Path> subtract = CollectionUtil.subtract(collect, badFileDir);
        for (Path dir : subtract) {
            Map<String, Object> map = fileUtils.rename(dir.getFileName().toString(), true);
            FileUtil.rename(dir, map.get("fileName").toString(), false);
        }
    }
}
