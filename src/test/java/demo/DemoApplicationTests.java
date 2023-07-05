package demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void contextLoads() {
		ResponseEntity<String> value = restTemplate.getForEntity("/", String.class);
		assertThat(value.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(value.getBody()).contains("mystérieux visiteur");
		assertThat(value.getHeaders().getContentType().toString()).isEqualTo("text/html;charset=UTF-8");
		assertThat(value.getHeaders().getContentLength()).isNotNull();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
