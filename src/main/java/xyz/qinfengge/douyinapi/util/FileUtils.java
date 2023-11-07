package xyz.qinfengge.douyinapi.util;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sun.activation.registries.MimeTypeFile;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lza
 * @date 2023/10/21-15:40
 **/

@Component
public class FileUtils {

    /**
     * 重命名文件
     * @param name 文件名
     * @return Map
     */
    public Map<String, Object> rename(String name, Boolean isDirectory) {
        String[] parts = name.split("_");

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

        List<String> collect = fileNameParts.stream().filter(v -> !v.isEmpty()).collect(Collectors.toList());

//        System.err.println(collect);


        // 当文件名为空时，即仅有日期时，使用日期
        String fileName;

        if (collect.size() == 1){
            fileName = collect.get(0);
        }else if (collect.size() == 2){
            if (isDirectory){
                fileName = collect.get(1);
            }else {
                fileName = collect.get(0) + "." + FileUtil.getSuffix(collect.get(1));
            }
        }else {
            // 当不是文件夹时重命名逻辑
            if (!isDirectory){
                File file = FileUtil.file(name);
                String suffix = FileUtil.getSuffix(file);
                // 当文件是图集时重命名逻辑
                if (!"jpg".equals(suffix)){
                    List<String> sublist = collect.subList(1, collect.size() - 1);
                    fileName = String.join(" ", sublist) + "." + suffix;
                }else {
                    List<String> sublist = collect.subList(1, collect.size() - 2);
                    fileName = String.join(" ", sublist) + collect.get(collect.size() - 1);
                }
            }else {
                List<String> sublist = collect.subList(1, collect.size());
                fileName = String.join(" ", sublist);
            }
        }


        Map<String, Object> map = new HashMap<>();
        map.put("fileName", fileName);
        map.put("tags", tags);
        map.put("created", collect.get(0));
        return map;
    }

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


    private final String[] imageSuffix = {"jpg", "gif", "jpge", "png", "webm"};

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
        for (String s : list) {
            if ("mp3".equals(FileUtil.getSuffix(s))){
                return true;
            }
        }
        return false;
    }

    public Boolean hasThumbnail(Path dir){
        List<String> list = FileUtil.listFileNames(dir.toString());
        for (String s : list) {
            return ObjectUtil.contains(imageSuffix, FileUtil.getSuffix(s));
        }
        return null;
    }


    /**
     * 判断文件夹是否合规，有图片或视频
     * @param dir 文件夹
     * @return 是否合规
     * @throws IOException IOException
     */
    @SneakyThrows
    public Boolean checkFileDir(Path dir){
        boolean niceDir = false;

        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
        for (Path file : stream) {
            String fileName = file.getFileName().toString();
            if (ObjectUtil.contains(videoSuffix, FileUtil.getSuffix(fileName))) {
                niceDir = true;
            }
            if (ObjectUtil.contains(imageSuffix, FileUtil.getSuffix(fileName))) {
                niceDir = true;
            }
        }
        stream.close();

        return niceDir;
    }

}
