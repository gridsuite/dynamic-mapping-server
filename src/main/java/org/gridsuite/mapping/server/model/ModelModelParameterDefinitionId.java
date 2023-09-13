package org.gridsuite.mapping.server.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class ModelModelParameterDefinitionId implements Serializable {
    @EqualsAndHashCode.Include
    @Column(name = "model_name")
    private String modelName;

    @EqualsAndHashCode.Include
    @Column(name = "parameter_definition_name")
    private String parameterDefinitionName;

}
