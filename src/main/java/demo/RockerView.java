package demo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.View;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.runtime.OutputStreamOutput;
import com.fizzed.rocker.runtime.RockerRuntime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RockerView implements View {

	private String prefix = "templates/";
	private String suffix = ".rocker.html";
	private String path;
	private Object[] arguments;

	public RockerView(String path, Object... arguments) {
		this.path = path.startsWith(prefix) ? path : prefix + path + suffix;
		this.arguments = arguments;
	}

	@Override
	public void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		BindableRockerModel template = Rocker.template(path, arguments);
		bind(template, model);
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

	private void bind(BindableRockerModel template, Map<String, ?> model) {
		RockerModel rocker = RockerRuntime.getInstance().getBootstrap().model(path);
		for (String name : getModelArgumentNames(rocker)) {
			if (model.containsKey(name)) {
				template.bind(name, model.get(name));
			}
		}
	}

	@Override
	@Nullable
	public String getContentType() {
		return MediaType.TEXT_HTML_VALUE;
	}

	static private String[] getModelArgumentNames(RockerModel model) {
		try {
			Method f = model.getClass().getMethod("getArgumentNames");
			return (String[]) f.invoke(null);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read getArgumentNames static method from template");
		}
	}

}
