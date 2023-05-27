package lsdi.fogworker.DataTransferObjects.Deploy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lsdi.fogworker.DataTransferObjects.RuleRequestResponse;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
public class DeployFogRequest extends DeployRequest {
    @Nullable
    @JsonProperty("edge_rules_deploy_requests")
    public List<DeployEdgeRequest> edgeRulesDeployRequests;
    @Nullable
    @JsonProperty("cloud_rules_deploy_requests")
    public List<DeployCloudRequest> cloudRulesDeployRequests;
    @Nullable
    @JsonProperty("fog_rules")
    public List<RuleRequestResponse> fogRules;
}
