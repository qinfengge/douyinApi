package xyz.qinfengge.douyinapi.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lza
 * @date 2023/10/21-15:40
 **/

@Component
public class FileUtils {


    /**
     * 获取文件父目录名称
     * @param dir 文件路径
     * @param level 层级
     * @return String
     */
    public String getFileParentName(Path dir, Integer level){
        String parent = FileUtil.getParent(String.valueOf(dir), level);
        return parent.substring(parent.lastIndexOf("\\") + 1);
    }

    /**
     * 常见视频后缀
     */
    private final String[] videoSuffix = {"mp4", "avi", "mov", "flv", "mkv", "3gp"};

    /**
     * 用来判断文件夹中是否包含有视频
     * 主要用于区分视频和图文
     * @param dir 文件夹路径
     * @return 是否包含视频
     * @throws IOException IOException
     */
    public Boolean containsVideo(Path dir) throws IOException {
        boolean containsVideo = false;

        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path file : stream) {
            String fileName = file.getFileName().toString();
            if (ObjectUtil.contains(videoSuffix, FileUtil.getSuffix(fileName))) {
                containsVideo = true;
            }
        }
        stream.close();

        return containsVideo;
    }


    private final String[] imageSuffix = {"jpg", "gif", "jpeg", "png", "webp"};

    public Map<String, String> getDirFileType(Path dir){
        List<String> list = FileUtil.listFileNames(dir.toString());
        Map<String, String> map = new HashMap<>();
        for (String fileName : list) {
            String suffix = FileUtil.getSuffix(fileName);

            if (ObjectUtil.contains(videoSuffix, suffix)) {
                map.putIfAbsent("videoSuffix", suffix);
            }

            if (ObjectUtil.contains(imageSuffix, suffix)) {
                map.putIfAbsent("imageSuffix", suffix);
            }
        }
        return map;
    }

    /**
     * 判断文件夹中是否包含有音频
     * @param list 文件夹中的文件列表
     * @return 是否包含音频
     */
    public Boolean hasAudio(List<String> list) {
        // 有原声时
        List<String> suffix = new ArrayList<>();
        for (String s : list) {
            suffix.add(FileUtil.getSuffix(s));
        }
        return ObjectUtil.contains(suffix, "mp3");
    }

    public Boolean hasThumbnail(Path dir){
        List<String> list = FileUtil.listFileNames(dir.toString());
        List<String> suffix = new ArrayList<>();
        for (String s : list) {
            suffix.add(FileUtil.getSuffix(s));
        }
        ArrayList<String> images = new ArrayList<>(Arrays.asList(imageSuffix));
        // 求交集，若交集为空，则不存在缩略图，否则存在缩略图
        return CollectionUtil.isNotEmpty(CollectionUtil.intersection(images, suffix));
    }


    /**
     * 判断文件夹是否合规，有图片或视频
     * @param dir 文件夹
     * @return 是否合规
     * @throws IOException IOException
     */
    @SneakyThrows
    public Boolean checkFileDir(Path dir){
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        List<String> suffix = new ArrayList<>();
        for (Path file : stream) {
            suffix.add(FileUtil.getSuffix(file.getFileName().toString()));
        }
        stream.close();

        ArrayList<String> images = new ArrayList<>(Arrays.asList(imageSuffix));
        ArrayList<String> videos = new ArrayList<>(Arrays.asList(videoSuffix));

        boolean hasVideo = CollectionUtil.isNotEmpty(CollectionUtil.intersection(images, suffix));
        boolean hasImage = CollectionUtil.isNotEmpty(CollectionUtil.intersection(videos, suffix));

        return hasVideo || hasImage;
    }


    /**
     * 根据文件夹名称获取信息
     * @param name 文件夹名称
     * @return Map
     */
    public Map<String, Object> splitDirInfo(String name, Boolean isDirectory){
        // 使用正则表达式匹配包含时间的完整日期
        Pattern datePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2})");
        Matcher dateMatcher = datePattern.matcher(name);
        String createTime = "";
        if (dateMatcher.find()) {
            createTime = dateMatcher.group(1);
        }

        // 如果是文件，去掉后缀
        if (!isDirectory){
            name = FileUtil.getPrefix(name);
        }
        // 分割出类型、作者和文件名
        String[] parts = name.replaceFirst(createTime, "").split("-");
        String type = parts.length > 1 ? parts[1] : "";
        String author = parts.length > 2 ? parts[2] : "";
        String fileName = parts.length > 3 ? parts[3] : "";
        String fileNameEncode = parts.length > 3 ? parts[3].replaceAll("#", "") : "";

        Map<String, Object> map = new HashMap<>();
        map.put("created", createTime);
        map.put("type", type);
        map.put("author", author);
        // 当文件名为空时，则使用日期和作者作为文件名
        map.put("fileName", (fileName.isEmpty() ? createTime + "_" + author : fileName));
        map.put("fileNameEncode", (fileNameEncode.isEmpty() ? createTime + "_" + author : fileNameEncode));

        // 收集标签
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#(.*?)(?=#|$)");
        Matcher matcher = pattern.matcher(fileName);
        while (matcher.find()) {
            // group(1) 引用的是第一个括号内的内容，即 (.*?)
            tags.add(matcher.group().trim());
        }

        map.put("tags", tags);

        return map;
    }

    /**
     * 图集的重命名逻辑
     * @param fullName 文件名
     * @return String
     */
    public String albumRename(String fullName){

        String[] parts = fullName.split("_");
        String name = parts[0];

        // 使用正则表达式匹配包含时间的完整日期
        Pattern datePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2})");
        Matcher dateMatcher = datePattern.matcher(name);
        String createTime = "";
        if (dateMatcher.find()) {
            createTime = dateMatcher.group(1);
        }

        // 分割出类型、作者和文件名
        String[] parts2 = name.replaceFirst(createTime, "").split("-");
        String fileName = parts2.length > 3 ? parts2[3] : "";

        if ("mp3".equals(FileUtil.getSuffix(fullName))){
            return fileName;
        }else {
            // 重命名后的文件名
            return fileName + "_" + parts[1];
        }
    }


}
