package lsdi.fogworker;

import lsdi.fogworker.DataTransferObjects.IoTGatewayRequest;
import lsdi.fogworker.DataTransferObjects.TaggedObjectRequest;
import lsdi.fogworker.Services.IotCatalogerService;
import lsdi.fogworker.Services.TaggerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
public class FogworkerApplication {
	@Value("${fogworker.uuid}")
	private String fogworkerUuid;
	@Value("${fogworker.name}")
	private String fogworkerName;
	@Value("${fogworker.url}")
	private String fogworkerUrl;
	@Value("${iotcataloger.url}")
	private String iotCatalogerUrl;
	@Value("${tagger.url}")
	private String taggerUrl;

	public static void main(String[] args) {
		SpringApplication.run(FogworkerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationEvent() {
		selfRegister();
//		selfTag();
	}

	private void selfRegister() {
		IotCatalogerService iotCatalogerService = new IotCatalogerService(iotCatalogerUrl);
		IoTGatewayRequest request = new IoTGatewayRequest();
		request.setUuid(fogworkerUuid);
		request.setDistinguishedName(fogworkerName);
		request.setUrl(fogworkerUrl);
		request.setLatitude(1.0);
		request.setLongitude(1.0);

		iotCatalogerService.registerGateway(request);
	}

	private void selfTag() {
		TaggerService taggerService = new TaggerService(taggerUrl);
		TaggedObjectRequest request = new TaggedObjectRequest();
		request.setUuid(fogworkerUuid);
		request.setType("FogNode");

		Map<String, String> tags = new HashMap<>();
		tags.put("type", "fognode");

		request.setTags(tags);
		taggerService.tagObject(request);
	}
}
