package xyz.qinfengge.douyinapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class DouyinApiApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void readFiles() {
		String path = "J:/dy";
		String ip = "http://v.gggg.plus/douyin/";
		File file = new File(path);
		String [] filelist = file.list();
		for (int i=0;i< filelist.length;i++){
			System.out.println(ip + filelist[i]);

		}
	}
}
