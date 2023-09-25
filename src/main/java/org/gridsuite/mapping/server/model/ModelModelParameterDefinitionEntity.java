package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.ParameterOrigin;

import jakarta.persistence.*;
import java.io.Serializable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "models_model_parameter_definitions")
public class ModelModelParameterDefinitionEntity implements Serializable {
    @EmbeddedId
    private ModelModelParameterDefinitionId id;

    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("modelName")
    private ModelEntity model;

    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @MapsId("parameterDefinitionName")
    private ModelParameterDefinitionEntity parameterDefinition;

    @Column(name = "origin")
    private ParameterOrigin origin;

    public ModelModelParameterDefinitionEntity(ModelEntity model, ModelParameterDefinitionEntity parameterDefinition, ParameterOrigin origin) {
        this.model = model;
        this.parameterDefinition = parameterDefinition;
        this.origin = origin;
        this.id = new ModelModelParameterDefinitionId(model.getModelName(), parameterDefinition.getName());
    }

}
