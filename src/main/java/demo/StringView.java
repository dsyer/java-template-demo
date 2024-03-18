package demo;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StringView implements View {

	private final Supplier<String> output;

	public StringView(Supplier<String> output) {
		this.output = output;
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String result = output.get();
		response.setContentType(MediaType.TEXT_HTML_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentLength(result.getBytes().length);

		response.getOutputStream().write(result.getBytes());
		response.flushBuffer();
	}
}
