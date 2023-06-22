/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public interface ModelService {

    List<Object> getAutomatonDefinitions();

    List<ModelParameterDefinition> getParametersDefinitionsFromModelName(String modelName);

    List<SimpleModel> getModels();

    List<ParametersSet> getSetsFromGroup(String modelName, String groupName, SetGroupType groupType);

    ParametersSetsGroup saveParametersSetsGroup(String modelName, ParametersSetsGroup setsGroup, Boolean strict);

    Model saveModel(Model model);

    ParametersSetsGroup deleteSet(String modelName, String groupName, SetGroupType groupType, String setName);

    // --- BEGIN variable-related service methods --- //
    Model addNewVariableDefinitionsToModel(String modelName, List<ModelVariableDefinition> variableDefinitions);

    Model addExistingVariableDefinitionsToModel(String modelName, List<String> variableDefinitionNames);

    Model removeExistingVariableDefinitionsFromModel(String modelName, List<String> variableDefinitionNames);

    List<ModelVariableDefinition> saveNewVariableDefinitions(List<ModelVariableDefinition> variableDefinitions);

    Model removeAllVariableDefinitionsOnModel(String modelName);

    VariablesSet saveNewVariablesSet(VariablesSet variableSet);

    VariablesSet addNewVariableDefinitionToVariablesSet(String variableSetName, List<ModelVariableDefinition> variableDefinitions);

    VariablesSet removeExistingVariableDefinitionFromVariablesSet(String variableSetName, List<String> variableDefinitionNames);

    VariablesSet removeAllVariableDefinitionOnVariablesSet(String variableSetName);

    Model addNewVariablesSetsToModel(String modelName, List<VariablesSet> variableSets);

    Model addExistingVariablesSetsToModel(String modelName, List<String> variablesSetNames);

    Model removeExistingVariablesSetsFromModel(String modelName, List<String> variablesSetNames);

    Model removeAllExistingVariablesSetsFromModel(String modelName);

    List<String> deleteVariableDefinitions(List<String> variableDefinitionNames);

    List<String> deleteVariablesSets(List<String> variablesSetNames);
    // --- BEGIN variable-related service methods --- //
}
