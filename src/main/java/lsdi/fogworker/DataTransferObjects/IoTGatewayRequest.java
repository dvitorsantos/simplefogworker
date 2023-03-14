package lsdi.fogworker.DataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IoTGatewayRequest {
    private String uuid;
    @JsonProperty("lat")
    private Double latitude;
    @JsonProperty("lon")
    private Double longitude;
    @JsonProperty("dn")
    private String distinguishedName;
    private String url;
}
