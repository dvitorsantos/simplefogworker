package lsdi.fogworker.Controllers;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lsdi.fogworker.DataTransferObjects.DeployRequest;
import lsdi.fogworker.DataTransferObjects.DeployResponse;
import lsdi.fogworker.Listeners.EventListener;
import lsdi.fogworker.Services.EsperService;
import lsdi.fogworker.Services.MqttService;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class DeployController {
    EsperService esperService = EsperService.getInstance();
    MqttService mqttService = MqttService.getInstance();

    ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/deploy")
    public Object deploy(@RequestBody DeployRequest deployRequest) {
        try {
            EPCompiled epCompiled = esperService.compile(EsperService.buildEPL(deployRequest));
            EPDeployment epDeployment = esperService.deploy(epCompiled);
            EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), deployRequest.getRuleName());

            deployRequest.getEdgeRulesUuids().forEach(rule_uuid -> {
                mqttService.subscribe("cdpo/event/" + rule_uuid, (topic, message) -> {
                    new Thread(() -> {
                        try {
                            esperService.sendEvent(mapper.readValue(message.getPayload(), Map.class), deployRequest.getEventType());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
            });

            epStatement.addListener(new EventListener());

            return new DeployResponse(
                    epDeployment.getDeploymentId(),
                    deployRequest.getRuleName(),
                    deployRequest.getRuleDefinition(),
                    "Deployed successfully.");
        } catch (EPCompileException | EPDeployException exception) {
            exception.printStackTrace();
            return "Something went wrong.";
        }
    }
}
