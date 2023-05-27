package lsdi.fogworker.Controllers;

import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import lsdi.fogworker.DataTransferObjects.Deploy.DeployFogRequest;
import lsdi.fogworker.DataTransferObjects.Undeploy.UndeployFogRequest;
import lsdi.fogworker.Services.DeployService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class DeployController {
    DeployService deployService = new DeployService();

    @PostMapping("/deploy")
    public Object deploy(@RequestBody DeployFogRequest deployRequest) throws EPDeployException, EPCompileException {
        return deployService.deploy(deployRequest);
    }

    @PostMapping("/undeploy")
    public void undeploy(@RequestBody List<UndeployFogRequest> undeployFogRequests) {
        undeployFogRequests.forEach(undeployFogRequest -> deployService.undeploy(undeployFogRequest));
    }
}
