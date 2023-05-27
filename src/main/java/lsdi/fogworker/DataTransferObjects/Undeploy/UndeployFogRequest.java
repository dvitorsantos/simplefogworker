package lsdi.fogworker.DataTransferObjects.Undeploy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UndeployFogRequest {
    @JsonProperty("edge_rules_deploy_uuids")
    public List<String> edgeRulesDeployUuids;
    @JsonProperty("fog_rules_deploy_uuids")
    public List<String> fogRulesDeployUuids;
}
