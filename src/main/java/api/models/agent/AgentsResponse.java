package api.models.agent;
import api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentsResponse extends BaseModel {
    private List<AgentResponse> agent;
}
