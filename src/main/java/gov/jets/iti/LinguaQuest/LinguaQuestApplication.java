package gov.jets.iti.LinguaQuest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LinguaQuestApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinguaQuestApplication.class, args);
	}

}
