package lsdi.fogworker.DataTransferObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeployResponse {
    public String id;
    public String name;
    public String rule;
    public String status;
}
