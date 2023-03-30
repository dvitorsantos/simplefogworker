package lsdi.fogworker.DataTransferObjects.Deploy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lsdi.fogworker.DataTransferObjects.RuleRequestResponse;

import java.util.List;

@Data
public class DeployFogRequest extends DeployRequest {
    @JsonProperty("edge_rules_deploy_requests")
    public List<DeployEdgeRequest> edgeRulesDeployRequests;
    @JsonProperty("fog_rules")
    public List<RuleRequestResponse> fogRules;
}
