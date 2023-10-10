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
import xyz.qinfengge.douyinapi.entity.Video;
import xyz.qinfengge.douyinapi.mapper.VideoMapper;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

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
		String [] filelist = file.list();
		for (int i=0;i< filelist.length;i++){
			String name = URLEncoder.encode(filelist[i], "UTF-8");
			System.out.println(ip + name);
		}
	}

	@Test
	void tt(){
		Integer [] arr = new Integer[925];
		for (int i=1;i< 926;i++){
			arr[i-1] = i;
		}
		System.err.println(Arrays.toString(arr));
	}

	@Test
	void exportJsonFile(){
		List<Video> videos = videoMapper.selectList(null);
		JSONArray objects = JSONUtil.parseArray(videos);
		ClassPathResource resource = new ClassPathResource("/");
		FileUtil.writeString(objects.toString(),resource.getPath(),"UTF-8");
	}

}
