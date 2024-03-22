package demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;

@Configuration
public class RockerAutoConfiguration {
	
	@Bean
	public ViewResolver rockerViewResolver() {
		return new RockerViewResolver();
	}

}
