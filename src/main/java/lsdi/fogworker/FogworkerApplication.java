package lsdi.fogworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class FogworkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FogworkerApplication.class, args);
	}
}
