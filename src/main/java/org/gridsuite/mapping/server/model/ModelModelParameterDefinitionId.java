package org.gridsuite.mapping.server.model;

import lombok.*;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class ModelModelParameterDefinitionId implements Serializable {
    @EqualsAndHashCode.Include
    private String modelName;

    @EqualsAndHashCode.Include
    private String parameterDefinitionName;

}
