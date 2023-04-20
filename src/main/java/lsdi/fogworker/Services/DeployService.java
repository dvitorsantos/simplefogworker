package lsdi.fogworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPDeployment;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.EPUndeployException;
import com.espertech.esper.runtime.client.util.Adapter;
import com.espertech.esper.runtime.client.util.AdapterState;
import com.espertech.esper.runtime.client.util.AdapterStateManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lsdi.fogworker.DataTransferObjects.Deploy.DeployFogRequest;
import lsdi.fogworker.DataTransferObjects.Deploy.DeployResponse;
import lsdi.fogworker.DataTransferObjects.RuleRequestResponse;
import lsdi.fogworker.DataTransferObjects.Undeploy.UndeployFogRequest;
import lsdi.fogworker.Listeners.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeployService {
    CDPOService cdpoService = new CDPOService(System.getenv("CDPO_URL"));
    EsperService esperService = EsperService.getInstance();
    MqttService mqttService = MqttService.getInstance();
    ObjectMapper mapper = new ObjectMapper();

    public List<DeployResponse> deploy(DeployFogRequest deployFogRequest) {
        List<DeployResponse> deployResponses = new ArrayList<>();
        for (RuleRequestResponse fogRule : deployFogRequest.getFogRules()) {
            new Thread(() -> {
                try {
                    EPCompiled epCompiled = esperService.compile(EsperService.buildEPL(fogRule));
                    EPDeployment epDeployment = esperService.deploy(epCompiled);
                    EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), fogRule.getName());

                    epStatement.addListener(new EventListener(fogRule.getUuid(), fogRule.getWebhookUrl()));

                    DeployResponse deployResponse = new DeployResponse(
                            epDeployment.getDeploymentId(),
                            fogRule.getUuid(),
                            "DONE");
                    cdpoService.updateDeploy(deployResponse);
                } catch (EPCompileException | EPDeployException exception) {
                    exception.printStackTrace();
                    DeployResponse deployResponse = new DeployResponse(
                            null,
                            fogRule.getUuid(),
                            "ERROR");
                    cdpoService.updateDeploy(deployResponse);
                }
            }).start();
        }

        //edge deploy
        deployFogRequest.getEdgeRulesDeployRequests().forEach(edgeRuleDeployRequest -> {
            edgeRuleDeployRequest.getEdgeRules().forEach(edgeRule -> {
                //subscribe to receive events from edge

                if (!deployFogRequest.getFogRules().isEmpty())
                    mqttService.subscribe("cdpo/event/" + edgeRule.getEventType(), (topic, message) -> {
                        for (RuleRequestResponse fogRule : deployFogRequest.getFogRules()) {
                            new Thread(() -> {
                                try {
                                    esperService.sendEvent(mapper.readValue(message.getPayload(), Map.class), fogRule.getEventType());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    });

                //subscribe deploy status of edge
                mqttService.subscribe("/deploy/" + edgeRule.getUuid(), (topic, message) -> {
                    new Thread(() -> {
                        try {
                            DeployResponse deployResponse = mapper.readValue(message.getPayload(), DeployResponse.class);
                            cdpoService.updateDeploy(deployResponse);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
            });

            try {
                mqttService.publish("/deploy", mapper.writeValueAsBytes(edgeRuleDeployRequest));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        return deployResponses;
    }

    public void undeploy(UndeployFogRequest undeployFogRequest) {
        //undeploy fog
        undeployFogRequest.getFogRulesDeployUuids().forEach(fogRuleDeployUuid -> {
            try {
                esperService.undeploy(fogRuleDeployUuid);
            } catch (EPUndeployException e) {
                e.printStackTrace();
            }
        });

        //undeploy edge
        undeployFogRequest.getEdgeRulesDeployUuids().forEach(edgeRuleDeployUuid -> {
            try {
                mqttService.publish("/undeploy", mapper.writeValueAsBytes(edgeRuleDeployUuid));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
