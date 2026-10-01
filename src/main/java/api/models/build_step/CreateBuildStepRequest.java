package api.models.build_step;

import api.generators.GeneratingRule;
import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBuildStepRequest extends BaseModel {
    @GeneratingRule(regex = "[a-zA-Z][a-zA-Z0-9_]{7}")
    private String id;
    @GeneratingRule(regex = "Build_Step_[A-Z][a-z]{3}")
    private String name;
    private String type;
    private Boolean disabled = false;
    private Properties properties;
}
