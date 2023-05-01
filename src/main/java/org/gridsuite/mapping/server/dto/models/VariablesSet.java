package org.gridsuite.mapping.server.dto.models;

import lombok.Data;
import org.gridsuite.mapping.server.model.ModelVariableSetEntity;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class VariablesSet {
    private String name;
    private List<ModelVariableDefinition> variableDefinitions;

    public VariablesSet(ModelVariableSetEntity variableSetEntity) {
        this.name = variableSetEntity.getName();
        this.variableDefinitions = variableSetEntity.getVariableDefinitions().stream().map(ModelVariableDefinition::new).collect(Collectors.toList());
    }

    public VariablesSet(String name, List<ModelVariableDefinition> variableDefinitions) {
        this.name = name;
        this.variableDefinitions = variableDefinitions;
    }
}
