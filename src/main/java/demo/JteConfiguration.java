package demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;

@Configuration
public class JteConfiguration {

	@Bean
	public ViewResolver jteViewResolve(TemplateEngine templateEngine) {
		return new JteViewResolver(templateEngine);
	}

	@Bean
	public TemplateEngine templateEngine() {
		return TemplateEngine.createPrecompiled(ContentType.Html);
	}
}
