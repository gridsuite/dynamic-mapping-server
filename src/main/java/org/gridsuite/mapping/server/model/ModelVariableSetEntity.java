package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.models.VariablesSet;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "model_variable_sets")
public class ModelVariableSetEntity implements Serializable {
    @Id
    @Column(name = "variable_set_name")
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "variablesSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModelVariableDefinitionEntity> variableDefinitions;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "models_model_variable_sets",
        joinColumns = {@JoinColumn(name = "variable_set_name")},
        inverseJoinColumns = {@JoinColumn(name = "model_name")}
    )
    private List<ModelEntity> models;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    public ModelVariableSetEntity(List<ModelEntity> models, VariablesSet variablesSet) {
        this.models = models;
        this.name = variablesSet.getName();
        this.variableDefinitions = variablesSet.getVariableDefinitions().stream().map(variableDefinition -> new ModelVariableDefinitionEntity(models, this, variableDefinition)).collect(Collectors.toList());
        this.lastModifiedDate = variablesSet.getLastModifiedDate();
    }
}
