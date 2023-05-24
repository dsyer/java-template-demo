package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

	@Autowired
	VisitsRepository visitsRepository;

	@GetMapping("/")
	public String view(Model model) {
		visitsRepository.add();
		model.addAttribute("model", new DemoModel("mystérieux visiteur", visitsRepository.get()));
		return "templates/demo.rocker.html";
	}
}
