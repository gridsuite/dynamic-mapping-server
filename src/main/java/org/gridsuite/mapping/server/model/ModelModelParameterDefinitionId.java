package org.gridsuite.mapping.server.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class ModelModelParameterDefinitionId implements Serializable {
    private UUID modelId;

    private UUID parameterDefinitionId;

}
