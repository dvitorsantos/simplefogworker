package lsdi.fogworker.DataTransferObjects;

import lombok.Data;
import lsdi.fogworker.Models.Location;
import lsdi.fogworker.Models.Performace;
import java.time.LocalDateTime;

@Data
public class ContextDataRequestResponse {
    private String hostUuid;
    private Location location;
    private Performace performace;
    private LocalDateTime timestamp;
}