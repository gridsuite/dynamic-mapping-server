package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class ModelModelParameterDefinitionId implements Serializable {
    @Column(name = "model_name")
    private String modelName;

    @Column(name = "parameter_definition_name")
    private String parameterDefinitionName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelModelParameterDefinitionId that = (ModelModelParameterDefinitionId) o;
        return Objects.equals(modelName, that.modelName) && Objects.equals(parameterDefinitionName, that.parameterDefinitionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName, parameterDefinitionName);
    }
}
