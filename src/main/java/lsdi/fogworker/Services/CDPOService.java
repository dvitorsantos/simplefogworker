package lsdi.fogworker.Services;

import lombok.Data;
import lsdi.fogworker.DataTransferObjects.Deploy.DeployResponse;
import org.springframework.web.client.RestTemplate;

@Data
public class CDPOService {
    private CDPOService instance;
    private String url;
    private final RestTemplate restTemplate;
    public CDPOService(String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    public void updateDeploy(DeployResponse deployResponse) {
        restTemplate.put(url + "/deploy/" + deployResponse.getRuleUuid(), deployResponse, DeployResponse.class);
    }
}
