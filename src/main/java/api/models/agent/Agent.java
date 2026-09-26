package api.models.agent;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Agent {

    private Integer id;
    private String name;
    private Boolean connected;
    private Boolean authorized;
    private Boolean enabled;
}
