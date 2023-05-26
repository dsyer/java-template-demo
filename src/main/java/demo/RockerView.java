package demo;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.View;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.runtime.OutputStreamOutput;

public class RockerView implements View {

	private String path;
	private Object[] arguments;

	public RockerView(String path, Object... arguments) {
		this.path = path;
		this.arguments = arguments;
	}

	@Override
	public void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		BindableRockerModel template = Rocker.template(path, arguments);
		if (model.containsKey("arguments")) {
			template.bind((Map<String, Object>) model.get("arguments"));
		}
		response.setContentType(getContentType() == null ? MediaType.TEXT_HTML_VALUE : getContentType());
		template.render((type, charset) -> {
			try {
				response.setCharacterEncoding(charset);
				return new OutputStreamOutput(type, response.getOutputStream(), charset);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		});
	}

	@Override
	@Nullable
	public String getContentType() {
		return MediaType.TEXT_HTML_VALUE;
	}

}
