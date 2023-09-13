package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.ParameterOrigin;

import javax.persistence.*;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "models_model_parameter_definitions")
public class ModelModelParameterDefinitionEntity {
    @EmbeddedId
    private ModelModelParameterDefinitionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("modelName")
    private ModelEntity model;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelModelParameterDefinitionEntity that = (ModelModelParameterDefinitionEntity) o;
        return Objects.equals(model, that.model) && Objects.equals(parameterDefinition, that.parameterDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, parameterDefinition);
    }
}
