package lsdi.fogworker.Controllers;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.*;
import lsdi.fogworker.DataTransferObjects.DeployRequest;
import lsdi.fogworker.DataTransferObjects.DeployResponse;
import lsdi.fogworker.Listeners.EventListener;
import lsdi.fogworker.Services.EsperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DeployController {
    @Autowired
    EsperService esperService;

    @PostMapping("/deploy")
    public Object deploy(@RequestBody DeployRequest deployRequest) {
        try {
            EPCompiled epCompiled = esperService.compile(EsperService.buildEPL(deployRequest));
            EPDeployment epDeployment = esperService.deploy(epCompiled);
            EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), deployRequest.getName());
            epStatement.addListener(new EventListener());
            return new DeployResponse(
                    epDeployment.getDeploymentId(),
                    deployRequest.getName(),
                    deployRequest.getRule(),
                    "Deployed successfully.");
        } catch (EPCompileException | EPDeployException exception) {
            exception.printStackTrace();
            return "Something went wrong.";
        }
    }
}
