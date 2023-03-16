package lsdi.fogworker;

import lsdi.fogworker.DataTransferObjects.IoTGatewayRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication
public class FogworkerApplication {
	@Value("${iotcataloger.url}")
	private String iotCatalogerUrl;

	public static void main(String[] args) {
		SpringApplication.run(FogworkerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationEvent() {
		selfRegister();
	}

	private void selfRegister() {
		RestTemplate restTemplate = new RestTemplate();

		IoTGatewayRequest request = new IoTGatewayRequest();
		request.setUuid("fogworker");
		request.setDistinguishedName("fogworker");
		request.setUrl("http://localhost:6969/");
		request.setLatitude(1.0);
		request.setLongitude(1.0);

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-SSL-Client-DN", request.getDistinguishedName());

		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		restTemplate.postForObject(iotCatalogerUrl + "/gateway", entity, IoTGatewayRequest.class);
	}
}
