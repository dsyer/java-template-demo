package demo;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.ViewResolver;

public class RockerAutoConfiguration {
	
	@Bean
	public ViewResolver rockerViewResolver() {
		return new RockerViewResolver();
	}

}
