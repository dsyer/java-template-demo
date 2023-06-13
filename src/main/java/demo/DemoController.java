package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;

import io.jstach.opt.spring.webmvc.JStachioModelView;

@Controller
public class DemoController {

	@Autowired
	VisitsRepository visitsRepository;

	@Value("${spring.profiles.active:PROFILE_NOT_SET}")
	private String profile;

	@ResponseBody
	@GetMapping(value = "/profile")
	public String profile() {
		return profile;
	}

	@GetMapping("/")
	public View view() {
		visitsRepository.add();
		return JStachioModelView.of(new DemoModel("mystérieux visiteur", visitsRepository.get()));
	}
}
