package demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.jstach.jstachio.JStachio;
import io.jstach.jstachio.Template;
import io.jstach.jstachio.spi.JStachioExtension;
import io.jstach.opt.spring.SpringJStachio;
import io.jstach.opt.spring.SpringJStachioExtension;
import io.jstach.opt.spring.web.JStachioHttpMessageConverter;
import io.jstach.opt.spring.webmvc.ViewSetupHandlerInterceptor;

@Configuration
public class JStachioConfiguration implements WebMvcConfigurer {

	private final JStachio jstachio;

	/**
	 * Configures based on the jstachio found by spring
	 * 
	 * @param jstachio the found jstachio
	 */
	@Autowired
	public JStachioConfiguration(JStachio jstachio) {
		this.jstachio = jstachio;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(0, new JStachioHttpMessageConverter(jstachio));
	}

	@Bean
	@SuppressWarnings("exports")
	public ViewSetupHandlerInterceptor viewSetupHandlerInterceptor(ApplicationContext context) {
		return new ViewSetupHandlerInterceptor(context);
	}

}

@Configuration
class SpringTemplateConfig {

	/**
	 * Do nothing constructor to placate jdk 18 javadoc
	 */
	public SpringTemplateConfig() {
	}

	@Bean
	public SpringJStachioExtension jstachioService(@SuppressWarnings("exports") Environment environment,
			List<Template<?>> templates) {
		return new SpringJStachioExtension(environment, templates);
	}

	@Bean
	public JStachio jstachio(List<JStachioExtension> services) {
		var js = new SpringJStachio(services);
		// We need this for the view mixins.
		JStachio.setStatic(() -> js);
		return js;
	}

}
