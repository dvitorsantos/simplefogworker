package lsdi.fogworker.Services;

import lsdi.fogworker.DataTransferObjects.ContextDataRequestResponse;
import org.springframework.web.client.RestTemplate;

public class ContextMatcherService {
    ContextMatcherService instance;
    private String url;
    private final RestTemplate restTemplate;
    public ContextMatcherService(String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    public void updateContext(ContextDataRequestResponse context) {
        restTemplate.postForObject(url + "/context", context, String.class);
    }
}
