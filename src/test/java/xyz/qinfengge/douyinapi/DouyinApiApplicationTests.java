package xyz.qinfengge.douyinapi;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.sun.deploy.net.URLEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.enums.Type;
import xyz.qinfengge.douyinapi.mapper.VideoMapper;
import xyz.qinfengge.douyinapi.util.AdvancedUtil;
import xyz.qinfengge.douyinapi.util.FileUtils;
import xyz.qinfengge.douyinapi.util.ThumbnailUtil;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@SpringBootTest
class DouyinApiApplicationTests {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private FileUtils fileUtils;

    @Test
    void contextLoads() {
    }

    @Test
    void readFiles() throws UnsupportedEncodingException {
        String path = "G:/dy";
        String ip = "https://v.gggg.plus/";
        File file = new File(path);
        String[] filelist = file.list();
        for (int i = 0; i < filelist.length; i++) {
            String name = URLEncoder.encode(filelist[i], "UTF-8");
            System.out.println(ip + name);
        }
    }

    @Test
    void tt() {
        Integer[] arr = new Integer[925];
        for (int i = 1; i < 926; i++) {
            arr[i - 1] = i;
        }
        System.err.println(Arrays.toString(arr));
    }

    @Test
    void exportJsonFile() {
        List<Video> videos = videoMapper.selectList(null);
        JSONArray objects = JSONUtil.parseArray(videos);
        ClassPathResource resource = new ClassPathResource("/");
        FileUtil.writeString(objects.toString(), resource.getPath(), "UTF-8");
    }


    @Resource
    private ThumbnailUtil thumbnailUtil;

    @Test
    void CVTest() throws InterruptedException {
        thumbnailUtil.singleVideoInsert();
        sleep(10 * 1000);
    }


    @Test
    void doRename() {
        String name = "2022-02-27 12.26.45_郁金香哪有姐姐的浴巾香_";
        Map<String, Object> rename = fileUtils.rename(name, true);
        System.out.println("文件名:" + rename.get("fileName"));
        System.out.println("标签:" + rename.get("tags"));
        System.out.println("创建日期:" + rename.get("created"));
    }

    private Map<String, Object> rename(String name) {
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

        List<String> sublist = collect.subList(1, collect.size());
        String fileName = String.join(" ", sublist);


        Map<String, Object> map = new HashMap<>();
        map.put("fileName", fileName);
        map.put("tags", tags);
        map.put("created", collect.get(0));
        return map;
    }

    @Test
    void generateImagesLink() throws UnsupportedEncodingException {
        String path = "G:\\dy\\images";
        String ip = "https://x.gggg.plus/images/";
        File file = new File(path);
        String[] filelist = file.list();
        for (int i = 0; i < filelist.length; i++) {
            File items = FileUtil.file(path + "\\" + filelist[i]);
            Map<String, Object> map = rename(items.getName());
            Object tags = map.get("tags");
            List<String> tagsList = JSONUtil.toList(JSONUtil.parseArray(tags), String.class);
            Video video = new Video();
            video.setName(map.get("fileName").toString());
            video.setTags(tagsList);
            video.setCreated(map.get("created").toString());

            List<String> images = new ArrayList<>();
            for (String item : items.list()) {
                File image = FileUtil.file(items.getParent() + "\\" + filelist[i] + "\\" + item);
                Map<String, Object> imageMap = rename(image.getName());
                String fileName = imageMap.get("fileName").toString();
                String name = map.get("fileName").toString() + "/" + fileName;

                if (FileUtil.getSuffix(image).equals("mp3")){
                    video.setAudio(ip + name);
                }else {
                    images.add(ip + name);
                }

                FileUtil.rename(image, fileName, false);
            }
            video.setImages(images);
            video.setType(Type.POST.getCode());
            videoMapper.insert(video);

            FileUtil.rename(items, map.get("fileName").toString(), true);
        }
    }

    @Resource
    private AdvancedUtil advancedUtil;

    @Test
    void walkFileTree(){
        StopWatch watch = new StopWatch();
        watch.start("文件树遍历");
        advancedUtil.walkFileTree("G:\\dy\\vv");
        watch.stop();
        System.err.println(watch.prettyPrint());
    }

    @Test
    void tt2(){
        List<String> list = new ArrayList<>();
        list.add("清风阁.txt");
        list.add("test.mp4");
        list.add("ttt.pdf");
        list.add("xxx.jpg");
        boolean bool = list.stream().anyMatch(r -> r.endsWith(".gif"));
        System.err.println("是否包含===" + bool);
    }


    private final String[] videoSuffix = {"mp4", "avi", "mov", "flv", "mkv", "3gp"};
    Boolean containsVideo(Path dir) throws IOException {
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

    @Test
    void tt3() throws IOException {
        Path path = Paths.get("G:\\dy\\vv\\like\\秦风戈\\2023-10-03 18.43.59__凡所有相___皆是虚妄_#芜湖方特华裳盛典");
        Boolean aBoolean = containsVideo(path);
        System.err.println(aBoolean);
    }

    @Test
    void tt4(){
        Path path = Paths.get("G:\\dy\\vv\\like\\秦风戈\\2023-10-05 12.55.40_石青____色_既然无人懂_孤独又何妨_#古风_#情感古风_#汉服_#国风古韵_#武侠风");
        Map<String, String> map = fileUtils.getDirFileType(path);
        System.err.println("视频类型为：" + map.get("videoSuffix"));
        System.err.println("图片类型为：" + map.get("imageSuffix"));
    }


    @Test
    void test4(){
        Path path = Paths.get("G:\\dy\\vv\\like\\秦风戈\\2023-01-09 21.44.38_#用硬曲召唤我的brother_#甜妹");
        File[] files = FileUtil.file(path.toString()).listFiles();
        assert files != null;
//        Optional<File> file = Arrays.stream(files).findFirst().filter(r -> "mp4".equals(FileUtil.getSuffix(r)));
        Optional<File> file = Arrays.stream(files).filter(r -> "mp4".equals(FileUtil.getSuffix(r))).findFirst();
        System.err.println(file);
    }

    @Test
    void fileCheck(){
        Path path = Paths.get("G:\\dy\\vv\\like\\秦风戈\\2023-07-18 18.19.24_回复__尤克星星的评论_蹲蹲马步_粗暴好用_#武术_#武当功夫_#传统文化_#道家文化_#道系青年__抖音小助手");
        Boolean aBoolean = fileUtils.checkFileDir(path);
        System.err.println(aBoolean);
    }

    @Test
    void testAudio(){
        Path path = Paths.get("G:\\dy\\vv\\like\\秦风戈\\2023-01-17 10.00.01__汲取榜样的力量_#电影推荐_#高分电影");
        List<String> list = FileUtil.listFileNames(path.toString());
        Boolean aBoolean = fileUtils.hasAudio(list);
        System.err.println(aBoolean);
    }
}
