package api.models.agent;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentEnabledInfoRequest extends BaseModel {

    private Boolean status;
    private CommentRequest comment;

    public AgentEnabledInfoRequest(Boolean status, String comment) {
        this.status = status;
        this.comment = new CommentRequest(comment);
    }
}
