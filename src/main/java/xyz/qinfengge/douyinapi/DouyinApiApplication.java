package xyz.qinfengge.douyinapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "xyz.qinfengge")
public class DouyinApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DouyinApiApplication.class, args);
	}

}
