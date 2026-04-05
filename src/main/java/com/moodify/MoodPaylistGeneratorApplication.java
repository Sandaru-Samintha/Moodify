package com.moodify;

import com.moodify.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoodPaylistGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MoodPaylistGeneratorApplication.class);
		app.addInitializers(new EnvConfig());
		app.run(args);

	}

}
