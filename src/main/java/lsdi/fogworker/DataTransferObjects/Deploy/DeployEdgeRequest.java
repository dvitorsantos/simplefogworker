package lsdi.fogworker.DataTransferObjects.Deploy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lsdi.fogworker.DataTransferObjects.RuleRequestResponse;

import java.util.List;

@Data
public class DeployEdgeRequest extends DeployRequest {
    @JsonProperty("edge_rules")
    public List<RuleRequestResponse> edgeRules;
}
