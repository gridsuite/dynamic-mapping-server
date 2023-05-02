package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.models.VariablesSet;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "variablesSet", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ModelVariableDefinitionEntity> variableDefinitions;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(
        name = "models_model_variable_sets",
        joinColumns = {@JoinColumn(name = "variable_set_name")},
        inverseJoinColumns = {@JoinColumn(name = "model_name")}
    )
    private Set<ModelEntity> models;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private Date updatedDate;

    public ModelVariableSetEntity(ModelEntity model, VariablesSet variablesSet) {
        this.models = model != null ? new LinkedHashSet<>(Arrays.asList(model)) : new LinkedHashSet<>();
        this.name = variablesSet.getName();
        this.variableDefinitions = variablesSet.getVariableDefinitions().stream().map(variableDefinition -> new ModelVariableDefinitionEntity(model, this, variableDefinition)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelVariableSetEntity that = (ModelVariableSetEntity) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
