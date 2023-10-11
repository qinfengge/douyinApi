package xyz.qinfengge.douyinapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = "xyz.qinfengge")
@EnableCaching
public class DouyinApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DouyinApiApplication.class, args);
	}

}
