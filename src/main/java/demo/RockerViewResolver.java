package demo;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

public class RockerViewResolver implements ViewResolver, Ordered {

	private String prefix = "templates/";
	private String suffix = ".rocker.html";

	@Override
	@Nullable
	public View resolveViewName(String viewName, Locale locale) throws Exception {
		RockerView view = new RockerView(prefix + viewName + suffix);
		return view;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 10;
	}

}
