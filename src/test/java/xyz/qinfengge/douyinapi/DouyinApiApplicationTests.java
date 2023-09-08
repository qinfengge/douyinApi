package xyz.qinfengge.douyinapi;

import com.sun.deploy.net.URLEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@SpringBootTest
class DouyinApiApplicationTests {

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

}
