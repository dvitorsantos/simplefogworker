package lsdi.fogworker.Services;

import lombok.Data;
import lsdi.fogworker.DataTransferObjects.IoTGatewayRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Data
public class IotCatalogerService {
    private static IotCatalogerService instance;
    private final RestTemplate restTemplate;
    private String url;
    public IotCatalogerService(String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    public void registerGateway(IoTGatewayRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-SSL-Client-DN", request.getDistinguishedName());

        HttpEntity<Object> entity = new HttpEntity<>(request, headers);
        restTemplate.postForObject(url + "/gateway", entity, IoTGatewayRequest.class);
    }
}
