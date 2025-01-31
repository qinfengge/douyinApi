package xyz.qinfengge.douyinapi;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.enums.Type;
import xyz.qinfengge.douyinapi.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author lza
 * @Date 2023/10/31/19/42
 **/
public class WalkFileTreeTest {

    private final static String SITE_URL = "https://x.gggg.plus/dy/";

    @SneakyThrows
    public void walkFileTree(String path){
        AtomicInteger dircount = new AtomicInteger();
        AtomicInteger filecount = new AtomicInteger();

        List<Video> videoList = new ArrayList<>();
        List<Path> pathList = new ArrayList<>();
        List<String> dirFileNames = new ArrayList<>();

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
                    Map<String, Object> map = rename(file.getFileName().toString(), false);
                    FileUtil.rename(file, map.get("fileName").toString(), false);
                    pathList.add(file.getParent());
                }
                filecount.incrementAndGet();
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.err.println("文件夹名：" + dir.getFileName());

                Map<String, Object> map = rename(dir.getFileName().toString(), true);
                System.out.println("文件名:" + map.get("fileName"));
                System.out.println("标签:" + map.get("tags"));
                System.out.println("创建日期:" + map.get("created"));

                List<String> list = FileUtil.listFileNames(dir.toString());
                List<File> paths = FileUtil.loopFiles(dir.toString()).stream().filter(r -> !r.isDirectory()).collect(Collectors.toList());
//                System.err.println("文件夹内文件：" + paths);

                // 当文件夹中有文件才更新文件夹名，需要上传文件夹内以账号命名的txt文件
                boolean bool = list.stream().anyMatch(r -> r.endsWith(".txt"));
                List<String> collect = list.stream().filter(r -> r.endsWith(".txt")).collect(Collectors.toList());

                if (!list.isEmpty() && !bool){
                    System.err.println("需要重命名的文件：" + list);
                    String parent1 = getFileParentName(dir, 1);
                    String parent2 = getFileParentName(dir, 2);

                    Video video = new Video();
                    Boolean containsVideo = containsVideo(dir);
                    video.setName((String) map.get("fileName"));
                    video.setCreated(map.get("created").toString());
                    List<String> tags = JSONUtil.toList(JSONUtil.parseArray(map.get("tags")), String.class);
                    video.setTags(tags);
                    video.setUserName(parent1);
                    video.setType(Type.getTypeCode(parent2));

                    // 当为视频时
                    String shortUrl = parent2 + "/" + parent1 + "/" + map.get("fileName").toString() + "/" + map.get("fileName").toString();
                    if (containsVideo){
                        Map<String, String> mapSuffix = getDirFileType(dir);

                        if (hasAudio(list)){
                            video.setAudio(SITE_URL + shortUrl + ".mp3");
                        }
                        video.setUrl(SITE_URL + shortUrl + "." + mapSuffix.get("videoSuffix"));
                        video.setThumbnail(SITE_URL + shortUrl + "." + mapSuffix.get("imageSuffix"));
                    }else {
                        // 当为图集时
                        List<String> images = new ArrayList<>();
                        // 当图集有原声时
                        for (int i = 0; i < list.size(); i++) {
                            if ("mp3".equals(FileUtil.getSuffix(list.get(i)))){
                                video.setAudio(SITE_URL + shortUrl + ".mp3");
                                images.add(SITE_URL + shortUrl + i + ".jpg");
                            }else {
                                images.add(SITE_URL + shortUrl + (i+1) + ".jpg");
                            }
                        }
                        video.setImages(images);
                    }
                    System.err.println("video===" + video);
                    dirFileNames.add(map.get("fileName").toString());
//                    Path fileName = FileUtil.rename(dir, map.get("fileName").toString(), false);
                    videoList.add(video);

                }


                return super.postVisitDirectory(dir, exc);
            }
        });
        System.out.println("文件夹数量："+dircount+"文件数量："+filecount);
        List<Path> collect = pathList.stream().distinct().collect(Collectors.toList());
        for (Path dir : collect) {
            Map<String, Object> map = rename(dir.getFileName().toString(), true);
            FileUtil.rename(dir, map.get("fileName").toString(), false);
        }
    }


    /**
     * 重命名文件
     * @param name 文件名
     * @return Map
     */
    public Map<String, Object> rename(String name, Boolean isDirectory) {
        String[] parts = name.split("-");

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
        map.put("created", parts[0]);
        map.put("type", parts[1]);
        map.put("author", parts[2]);
        map.put("fileName", parts[3]);
        map.put("tags", tags);
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
            return "mp3".equals(FileUtil.getSuffix(s));
        }
        return null;
    }


    @Test
    void tt(){
        walkFileTree("G:\\dy\\vv");
    }

    @Test
    void doRename() {
        String name = "2024-03-26 19.56.57-视频-许益-在杭州最江南的樱花日落！#春日樱花大赏 #杭州樱花 #杭州路边偶遇樱花林 #好看的花一定要分享给你 #这是属于春天的氛围感";
        Map<String, Object> rename = rename(name, true);
        System.out.println("文件名:" + rename.get("fileName"));
        System.out.println("标签:" + rename.get("tags"));
        System.out.println("创建日期:" + rename.get("created"));
    }

    @Test
    void rr1(){
        String name = "2024-03-28 16.01.08-视频-汤阮-";

        // 使用正则表达式匹配包含时间的完整日期
        Pattern datePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2})");
        Matcher dateMatcher = datePattern.matcher(name);
        String createTime = "";
        if (dateMatcher.find()) {
            createTime = dateMatcher.group(1);
        }

        // 分割出类型、作者和文件名
        String[] parts = name.replaceFirst(createTime, "").split("-");
        String type = parts.length > 1 ? parts[1] : "";
        String author = parts.length > 2 ? parts[2] : "";
        String fileName = parts.length > 3 ? parts[3] : "";

        System.out.println("创建时间: " + createTime);
        System.out.println("类型: " + type);
        System.out.println("作者: " + author);
        System.out.println("文件名: " + (fileName.isEmpty() ? createTime + "_" + author : fileName));

        // 收集标签
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#(.*?)(?=#|$)");
        Matcher matcher = pattern.matcher(fileName);
        while (matcher.find()) {
            // group(1) 引用的是第一个括号内的内容，即 (.*?)
            tags.add(matcher.group().trim());
        }

        System.out.println("标签集合: " + tags);
    }

    @Test
    void rr2(){
        String name = "2024-02-20 10.45.33-图集-兔娘-旅…旅行者你说的是要这样踩你吗#原神#原神枫丹#原神海灯节_1.jpeg";

        String prefix = FileUtil.getPrefix(name);

        System.err.println(prefix);

        // 使用正则表达式匹配包含时间的完整日期
        Pattern datePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}\\.\\d{2}\\.\\d{2})");
        Matcher dateMatcher = datePattern.matcher(name);
        String createTime = "";
        if (dateMatcher.find()) {
            createTime = dateMatcher.group(1);
        }

        // 分割出类型、作者和文件名
        String[] parts = prefix.replaceFirst(createTime, "").split("-");
        String type = parts.length > 1 ? parts[1] : "";
        String author = parts.length > 2 ? parts[2] : "";
        String fileName = parts.length > 3 ? parts[3] : "";

        System.out.println("创建时间: " + createTime);
        System.out.println("类型: " + type);
        System.out.println("作者: " + author);
        System.out.println("文件名: " + (fileName.isEmpty() ? createTime + "_" + author : fileName));

        // 收集标签
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#(.*?)(?=#|$)");
        Matcher matcher = pattern.matcher(fileName);
        while (matcher.find()) {
            // group(1) 引用的是第一个括号内的内容，即 (.*?)
            tags.add(matcher.group().trim());
        }

        System.out.println("标签集合: " + tags);
    }

    @Test
    void rr3(){
        String[] imageSuffix = {"jpg", "gif", "jpge", "png", "webp"};
        List<String> suffix = new ArrayList<>();
        suffix.add("mp3");
        suffix.add("mp4");
        ArrayList<String> list1 = new ArrayList<>(Arrays.asList(imageSuffix));
        System.out.println(CollectionUtil.isNotEmpty(CollectionUtil.intersection(list1, suffix)));
    }

    @Test
    void rr4(){
        String path = "E:\\dy\\2024-02-20 10.45.33-图集-兔娘-旅…旅行者你说的是要这样踩你吗#原神#原神枫丹#原神海灯节";
        Path file = Paths.get(path);
        Boolean b = new FileUtils().checkFileDir(file);
        System.err.println(b);
    }

    @Test
    void rr5(){
        String fullName = "2024-02-20 10.45.33-图集-兔娘-旅…旅行者你说的是要这样踩你吗#原神#原神枫丹#原神海灯节_1.jpeg";
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
        String fileName = parts2.length > 3 ? parts2[3].replaceAll("#", "_") : "";

        if ("mp3".equals(FileUtil.getSuffix(fullName))){
            System.err.println(fileName);
        }else {
            // 重命名后的文件名
            System.err.println(fileName + "_" + parts[1]);
        }
    }
}
