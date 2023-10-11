package xyz.qinfengge.douyinapi;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.ResourceUtil;
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
import xyz.qinfengge.douyinapi.mapper.VideoMapper;
import xyz.qinfengge.douyinapi.util.ThumbnailUtil;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

@SpringBootTest
class DouyinApiApplicationTests {

    @Resource
    private VideoMapper videoMapper;

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
		String name = "2023-09-16 19.19.23_问一下大家🤔_配置一台5000元的电脑大概需要多少元#提问挑战_video.mp4";

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

		String[] split = name.split("\\.");
        List<String> sublist = collect.subList(1, collect.size() - 1);
		String fileName = String.join(" ", sublist) + "." + split[split.length - 1];

		System.out.println("文件名:" + fileName);
		System.out.println("标签:" + tags);
    }

}
