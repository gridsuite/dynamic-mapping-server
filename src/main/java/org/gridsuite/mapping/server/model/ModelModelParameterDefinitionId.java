package org.gridsuite.mapping.server.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class ModelModelParameterDefinitionId implements Serializable {
    @EqualsAndHashCode.Include
    private UUID modelId;

    @EqualsAndHashCode.Include
    private UUID parameterDefinitionId;

}
