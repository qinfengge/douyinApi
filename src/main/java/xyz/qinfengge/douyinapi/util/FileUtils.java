package xyz.qinfengge.douyinapi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Component;

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
    public Map<String, Object> rename(String name) {
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

        System.err.println(collect);


        // 当文件名为空时，即仅有日期时，使用日期
        String fileName;
        if (collect.size() > 1){
            List<String> sublist = collect.subList(1, collect.size());
            fileName = String.join(" ", sublist);
        }else {
            fileName = collect.get(0);
        }


        Map<String, Object> map = new HashMap<>();
        map.put("fileName", fileName);
        map.put("tags", tags);
        map.put("created", collect.get(0));
        return map;
    }

    /**
     * 获取文件父目录名称
     * @param path 文件路径
     * @param level 层级
     * @return String
     */
    public String getFileParentName(String path, Integer level){
        String parent = FileUtil.getParent(path, level);
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
            if (ObjectUtil.contains(FileUtil.getSuffix(fileName), videoSuffix)) {
                containsVideo = true;
            }
        }
        stream.close();

        return containsVideo;
    }

}
