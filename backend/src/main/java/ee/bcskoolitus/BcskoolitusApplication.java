package ee.bcskoolitus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BcskoolitusApplication {

	public static void main(String[] args) {
		SpringApplication.run(BcskoolitusApplication.class, args);
	}

}
