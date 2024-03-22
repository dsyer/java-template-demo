package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.jstach.jstache.JStachePath;

@SpringBootApplication
@JStachePath(prefix = "templates/", suffix = ".mustache")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}