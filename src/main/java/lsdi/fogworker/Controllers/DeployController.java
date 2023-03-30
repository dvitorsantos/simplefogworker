package lsdi.fogworker.Controllers;

import lsdi.fogworker.DataTransferObjects.Deploy.DeployFogRequest;
import lsdi.fogworker.DataTransferObjects.Undeploy.UndeployFogRequest;
import lsdi.fogworker.Services.DeployService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class DeployController {
    DeployService deployService = new DeployService();

    @PostMapping("/deploy")
    public Object deploy(@RequestBody DeployFogRequest deployRequest) {
        return deployService.deploy(deployRequest);
    }

    @PostMapping("/undeploy")
    public void undeploy(@RequestBody List<UndeployFogRequest> undeployFogRequests) {
        undeployFogRequests.forEach(undeployFogRequest -> deployService.undeploy(undeployFogRequest));
    }
}
