package org.gridsuite.mapping.server.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.ParameterOrigin;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_model_parameter_definition")
public class ModelModelParameterDefinitionEntity implements Serializable {
    @EmbeddedId
    private ModelModelParameterDefinitionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "model_model_parameter_definition_model_id_fk"))
    @MapsId("modelId")
    private ModelEntity model;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "parameter_definition_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "model_model_parameter_definition_parameter_definition_id_fk"))
    @MapsId("parameterDefinitionId")
    private ModelParameterDefinitionEntity parameterDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin")
    private ParameterOrigin origin;

    @Column(name = "origin_name")
    private String originName;

    public ModelModelParameterDefinitionEntity(ModelEntity model, ModelParameterDefinitionEntity parameterDefinition, ParameterOrigin origin, String originName) {
        this.model = model;
        this.parameterDefinition = parameterDefinition;
        this.origin = origin;
        this.originName = originName;
        this.id = new ModelModelParameterDefinitionId(model.getId(), parameterDefinition.getId());
    }

}
