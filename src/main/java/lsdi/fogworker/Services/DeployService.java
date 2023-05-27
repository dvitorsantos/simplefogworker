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
import lsdi.fogworker.DataTransferObjects.Deploy.DeployCloudRequest;
import lsdi.fogworker.DataTransferObjects.Deploy.DeployFogRequest;
import lsdi.fogworker.DataTransferObjects.Deploy.DeployResponse;
import lsdi.fogworker.DataTransferObjects.IoTGatewayRequest;
import lsdi.fogworker.DataTransferObjects.RuleRequestResponse;
import lsdi.fogworker.DataTransferObjects.Undeploy.UndeployFogRequest;
import lsdi.fogworker.Listeners.EventListener;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeployService {
    CDPOService cdpoService = new CDPOService(System.getenv("CDPO_URL"));
    EsperService esperService = EsperService.getInstance();
    MqttService mqttService = MqttService.getInstance();
    IotCatalogerService iotCatalogerService = new IotCatalogerService(System.getenv("IOTCATALOGER_URL"));
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();

    public List<DeployResponse> deploy(DeployFogRequest deployFogRequest) throws EPCompileException, EPDeployException {
        List<DeployResponse> deployResponses = new ArrayList<>();
        EPCompiled epCompiled = esperService.compile(esperService.buildEPL(deployFogRequest.getFogRules()));
        EPDeployment epDeployment = esperService.deploy(epCompiled);

        //deploy cloud
        if (!deployFogRequest.getCloudRulesDeployRequests().isEmpty()) {
            IoTGatewayRequest cloudGatewayUrl = iotCatalogerService.getGateway(deployFogRequest.getCloudRulesDeployRequests().get(0).getHostUuid());
            deployFogRequest.getCloudRulesDeployRequests().forEach(cloudRuleDeployRequest -> {
                new Thread(() -> {
                    restTemplate.postForObject(cloudGatewayUrl.getUrl() + "/deploy", cloudRuleDeployRequest, Object.class);
                }).start();
            });
            deployFogRequest.getFogRules().forEach(fogRule -> {
                new Thread(() -> {
                   if (fogRule.getTarget().equals("CLOUD")) {
                       restTemplate.postForObject(cloudGatewayUrl.getUrl() + "/subscribe/" + fogRule.getOutputEventType(), null, Object.class);
                   }
                }).start();
            });
        }

        //deploy fog
        for (RuleRequestResponse fogRule : deployFogRequest.getFogRules()) {
            new Thread(() -> {
                try {
                    EPStatement epStatement = esperService.getStatement(epDeployment.getDeploymentId(), fogRule.getName());
                    epStatement.addListener(new EventListener(fogRule.getOutputEventType(), fogRule.getWebhookUrl()));

                    DeployResponse deployResponse = new DeployResponse(
                            deployFogRequest.getHostUuid(),
                            epDeployment.getDeploymentId(),
                            fogRule.getUuid(),
                            "DEPLOYED");
                    cdpoService.updateDeploy(deployResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        //edge deploy
        deployFogRequest.getEdgeRulesDeployRequests().forEach(edgeRuleDeployRequest -> {
            edgeRuleDeployRequest.getEdgeRules().forEach(edgeRule -> {
                //subscribe to receive events from edge

                ArrayList<String> fogEventTypes = new ArrayList<>();
                if (!deployFogRequest.getFogRules().isEmpty()) {
                    deployFogRequest.getFogRules().forEach(fogRule -> {
                        if (!fogEventTypes.contains(fogRule.getEventType())) {
                            fogEventTypes.add(fogRule.getEventType());
                        }
                    });

                    fogEventTypes.forEach(fogEventType -> {
                        mqttService.subscribe("cdpo/event/" + edgeRule.getOutputEventType(), (topic, message) -> {
                            new Thread(() -> {
                                try {
                                    esperService.sendEvent(mapper.readValue(message.getPayload(), Map.class), edgeRule.getOutputEventType());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        });
                    });
                }

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

            new Thread(() -> {
                try {
                    mqttService.publish("/deploy/" + edgeRuleDeployRequest.getHostUuid(), mapper.writeValueAsBytes(edgeRuleDeployRequest));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).start();
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
