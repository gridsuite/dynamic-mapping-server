package org.gridsuite.mapping.server.dto.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.gridsuite.mapping.server.model.ModelVariableSetEntity;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class VariablesSet {
    private String name;
    private List<ModelVariableDefinition> variableDefinitions;
    @JsonIgnore
    private Date lastModifiedDate;

    public VariablesSet(ModelVariableSetEntity variableSetEntity) {
        this.name = variableSetEntity.getName();
        this.variableDefinitions = variableSetEntity.getVariableDefinitions().stream().map(ModelVariableDefinition::new).collect(Collectors.toList());
        lastModifiedDate = variableSetEntity.getLastModifiedDate();
    }

    public VariablesSet(String name, List<ModelVariableDefinition> variableDefinitions) {
        this.name = name;
        this.variableDefinitions = variableDefinitions;
        lastModifiedDate = new Date();
    }
}
