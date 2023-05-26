package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.View;

@Controller
public class DemoController {

	@Autowired
	VisitsRepository visitsRepository;

	@GetMapping("/")
	public View view() {
		visitsRepository.add();
		return new RockerView("demo", new DemoModel("mystérieux visiteur", visitsRepository.get()));
	}
}
