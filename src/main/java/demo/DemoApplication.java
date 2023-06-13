package demo;

import java.time.LocalDate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.jstach.jstache.JStacheCatalog;
import io.jstach.jstache.JStacheFlags;
import io.jstach.jstache.JStacheFlags.Flag;
import io.jstach.jstache.JStacheFormatterTypes;
import io.jstach.jstache.JStachePath;

@SpringBootApplication
@JStachePath(prefix = "templates/", suffix = ".mustache")
@JStacheFlags(flags = Flag.DEBUG)
@JStacheFormatterTypes(types = LocalDate.class)
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}