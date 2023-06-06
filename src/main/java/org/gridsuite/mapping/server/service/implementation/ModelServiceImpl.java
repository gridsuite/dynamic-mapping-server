/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.ModelParameterDefinitionRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ModelVariableRepository;
import org.gridsuite.mapping.server.repository.ModelVariablesSetRepository;
import org.gridsuite.mapping.server.service.ModelService;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class ModelServiceImpl implements ModelService {

    public static final String MODEL_NOT_FOUND = "Model not found: ";
    public static final String VARIABLES_SET_NOT_FOUND = "Variables set not found: ";

    private final ModelRepository modelRepository;
    private final ModelParameterDefinitionRepository modelParameterDefinitionRepository;
    private final ModelVariableRepository modelVariableRepository;
    private final ModelVariablesSetRepository modelVariablesSetRepository;

    @Autowired
    public ModelServiceImpl(
            ModelRepository modelRepository,
            ModelParameterDefinitionRepository modelParameterDefinitionRepository,
            ModelVariableRepository modelVariableRepository,
            ModelVariablesSetRepository modelVariablesSetRepository
    ) {
        this.modelRepository = modelRepository;
        this.modelParameterDefinitionRepository = modelParameterDefinitionRepository;
        this.modelVariableRepository = modelVariableRepository;
        this.modelVariablesSetRepository = modelVariablesSetRepository;
    }

    private ModelEntity getModelToUpdate(String modelName, Optional<ModelEntity> foundModelOpt) {
        return foundModelOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, MODEL_NOT_FOUND + modelName));
    }

    @Override
    public List<SimpleModel> getModels() {
        return modelRepository.findAll().stream().map(modelEntity -> new SimpleModel(new Model(modelEntity))).collect(Collectors.toList());
    }

    @Override
    public List<ParametersSet> getSetsFromGroup(String modelName, String groupName, SetGroupType groupType) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);
        if (foundModel.isPresent()) {
            try {
                return foundModel.get().getSetsGroups().stream().map(ParametersSetsGroup::new).filter(parametersSetsGroup -> parametersSetsGroup.getName().equals(groupName) && parametersSetsGroup.getType() == groupType).findAny().orElseThrow().getSets();
            } catch (NoSuchElementException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No group found with this type");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No model found with this name");
        }
    }

    @Override
    public ParametersSetsGroup saveParametersSetsGroup(String modelName, ParametersSetsGroup setsGroup, Boolean strict) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);
        if (foundModel.isPresent()) {
            ModelEntity modelToUpdate = foundModel.get();
            List<ModelSetsGroupEntity> savedGroups = modelToUpdate.getSetsGroups();
            ModelSetsGroupEntity previousGroup = savedGroups.stream().filter(savedGroup -> savedGroup.getName().equals(setsGroup.getName())).findAny().orElse(null);
            ModelSetsGroupEntity groupToAdd = new ModelSetsGroupEntity(modelToUpdate, setsGroup);
            groupToAdd.getSets().forEach(set -> set.setLastModifiedDate(new Date()));

            if (previousGroup == null) {
                savedGroups.add(groupToAdd);
            } else {
                // If additional checks are required here, ensure that set erasure cannot happen here with sets merging.
                groupToAdd.getSets().forEach(set ->
                        previousGroup.getSets().add(set)
                );
            }
            if (new Model(modelToUpdate).isParameterSetGroupValid(setsGroup.getName(), strict)) {
                modelRepository.save(modelToUpdate);
                return setsGroup;
            } else {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public Model saveModel(Model model) {
        modelRepository.save(new ModelEntity(model));
        return model;
    }

    @Override
    public ParametersSetsGroup deleteSet(String modelName, String groupName, SetGroupType groupType, String setName) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);
        if (foundModel.isPresent()) {
            ModelEntity modelToEdit = foundModel.get();
            ModelSetsGroupEntity setsGroup = modelToEdit.getSetsGroups().stream()
                    .filter(setGroup -> setGroup.getName().equals(groupName) && setGroup.getType().equals(groupType))
                    .findAny()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No group found"));
            List<ModelParameterSetEntity> sets = setsGroup.getSets();
            setsGroup.setSets(sets.stream().filter(set -> !set.getName().equals(setName)).collect(Collectors.toList()));
            modelRepository.save(modelToEdit);
            return new ParametersSetsGroup(setsGroup);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No model found with this name");
        }
    }

    // --- BEGIN parameter definition-related service methods --- //
    @Override
    public List<ModelParameterDefinition> getParameterDefinitionsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelEntity = getModelToUpdate(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getParameterDefinitions();
    }

    @Override
    public Model addNewParameterDefinitionsToModel(String modelName, List<ModelParameterDefinition> parameterDefinitions) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of parameter definitions
        if (parameterDefinitions != null && !parameterDefinitions.isEmpty()) {
            // do merge with existing list
            List<ModelParameterDefinitionEntity> parameterDefinitionEntities = parameterDefinitions.stream()
                    .map(parameterDefinition -> new ModelParameterDefinitionEntity(modelToUpdate, parameterDefinition))
                    .collect(Collectors.toList());
            modelToUpdate.addParameterDefinitions(parameterDefinitionEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    public Model addExistingParameterDefinitionsToModel(String modelName, List<String> parameterDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of parameter definitions
        if (parameterDefinitionNames != null && !parameterDefinitionNames.isEmpty()) {
            // find existing parameter definitions
            List<ModelParameterDefinitionEntity> foundParameterDefinitionEntities = modelParameterDefinitionRepository.findAllById(parameterDefinitionNames);

            // check whether found all, fail fast
            if (foundParameterDefinitionEntities.size() != parameterDefinitionNames.size()) {
                List<String> foundNames = foundParameterDefinitionEntities.stream().map(ModelParameterDefinitionEntity::getName).collect(Collectors.toList());
                List<String> notFoundNames = parameterDefinitionNames.stream().filter(name -> !foundNames.contains(name)).collect(Collectors.toList());
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some parameter definition not found: " + notFoundNames);
            }

            // do merge with existing list
            modelToUpdate.addParameterDefinitions(foundParameterDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    public Model removeExistingParameterDefinitionsFromModel(String modelName, List<String> parameterDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do remove in the list of parameter definitions
        if (parameterDefinitionNames != null && !parameterDefinitionNames.isEmpty()) {
            // find existing variable definitions
            List<ModelParameterDefinitionEntity> foundParameterDefinitionEntities = modelParameterDefinitionRepository.findAllById(parameterDefinitionNames);

            // remove in existing list
            modelToUpdate.removeParameterDefinitions(foundParameterDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    public Model removeAllParameterDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // clear the existing list
        modelToUpdate.removeParameterDefinitions(modelToUpdate.getParameterDefinitions());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    public List<ModelParameterDefinition> saveNewParameterDefinitions(List<ModelParameterDefinition> parameterDefinitions) {
        if (parameterDefinitions != null && !parameterDefinitions.isEmpty()) {
            Set<ModelParameterDefinitionEntity> parameterDefinitionEntities = parameterDefinitions.stream().map(variableDefinition -> new ModelParameterDefinitionEntity(null, variableDefinition)).collect(Collectors.toCollection(LinkedHashSet::new));
            List<ModelParameterDefinitionEntity> savedParameterDefinitionEntities = modelParameterDefinitionRepository.saveAll(parameterDefinitionEntities);
            return savedParameterDefinitionEntities.stream().map(ModelParameterDefinition::new).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public List<String> deleteParameterDefinitions(List<String> parameterDefinitionNames) {
        if (parameterDefinitionNames != null && !parameterDefinitionNames.isEmpty()) {
            modelParameterDefinitionRepository.deleteAllById(parameterDefinitionNames);
        }
        return parameterDefinitionNames;
    }
    // --- END parameter definition-related service methods --- //

    // --- BEGIN variable-related service methods --- //
    @Override
    @Transactional
    public Model addNewVariableDefinitionsToModel(String modelName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of variable definitions
        if (variableDefinitions != null && !variableDefinitions.isEmpty()) {
            // do merge with existing list
            List<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream()
                    .map(variableDefinition -> new ModelVariableDefinitionEntity(modelToUpdate, null, variableDefinition))
                    .collect(Collectors.toList());
            modelToUpdate.addVariableDefinitions(variableDefinitionEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    @Transactional
    public Model addExistingVariableDefinitionsToModel(String modelName, List<String> variableDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of variable definitions
        if (variableDefinitionNames != null && !variableDefinitionNames.isEmpty()) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // check whether found all, fail fast
            if (foundVariableDefinitionEntities.size() != variableDefinitionNames.size()) {
                List<String> foundNames = foundVariableDefinitionEntities.stream().map(ModelVariableDefinitionEntity::getName).collect(Collectors.toList());
                List<String> notFoundNames = variableDefinitionNames.stream().filter(name -> !foundNames.contains(name)).collect(Collectors.toList());
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some variable definition not found: " + notFoundNames);
            }

            // do merge with existing list
            modelToUpdate.addVariableDefinitions(foundVariableDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model removeExistingVariableDefinitionsFromModel(String modelName, List<String> variableDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do remove in the list of variable definitions
        if (variableDefinitionNames != null && !variableDefinitionNames.isEmpty()) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // remove in existing list
            modelToUpdate.removeVariableDefinitions(foundVariableDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public List<ModelVariableDefinition> saveNewVariableDefinitions(List<ModelVariableDefinition> variableDefinitions) {
        if (variableDefinitions != null && !variableDefinitions.isEmpty()) {
            Set<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream().map(variableDefinition -> new ModelVariableDefinitionEntity(null, null, variableDefinition)).collect(Collectors.toCollection(LinkedHashSet::new));
            List<ModelVariableDefinitionEntity> savedVariableDefinitionEntities = modelVariableRepository.saveAll(variableDefinitionEntities);
            return savedVariableDefinitionEntities.stream().map(ModelVariableDefinition::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Model removeAllVariableDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

         // clear the existing list
        modelToUpdate.removeVariableDefinitions(modelToUpdate.getVariableDefinitions());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet saveNewVariablesSet(VariablesSet variableSet) {
        ModelVariableSetEntity variableSetEntity = new ModelVariableSetEntity(null, variableSet);
        ModelVariableSetEntity savedVariableSetEntity = modelVariablesSetRepository.save(variableSetEntity);
        return new VariablesSet(savedVariableSetEntity);
    }

    @Override
    @Transactional
    public VariablesSet addNewVariableDefinitionToVariablesSet(String variableSetName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelVariableSetEntity> foundModelOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = foundModelOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, VARIABLES_SET_NOT_FOUND + variableSetName));

        if (variableDefinitions != null && !variableDefinitions.isEmpty()) {
            // do merge with existing list
            List<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream().map(variableDefinition -> new ModelVariableDefinitionEntity(null, variableSetToUpdate, variableDefinition)).collect(Collectors.toList());
            variableSetToUpdate.addVariableDefinitions(variableDefinitionEntities);
            // save modified existing variables set entity
            modelVariablesSetRepository.save(variableSetToUpdate);
        }

        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet removeExistingVariableDefinitionFromVariablesSet(String variableSetName, List<String> variableDefinitionNames) {
        Optional<ModelVariableSetEntity> foundModelOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = foundModelOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, VARIABLES_SET_NOT_FOUND + variableSetName));

        if (variableDefinitionNames != null && !variableDefinitionNames.isEmpty()) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // remove in existing list
            variableSetToUpdate.removeVariableDefinitions(foundVariableDefinitionEntities);

            // save modified existing variables set entity
            // variable definitions are systematically deleted via orphanRemoval = true
            modelVariablesSetRepository.save(variableSetToUpdate);
        }

        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet removeAllVariableDefinitionOnVariablesSet(String variableSetName) {
        Optional<ModelVariableSetEntity> foundModelOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = foundModelOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, VARIABLES_SET_NOT_FOUND + variableSetName));

        // clear the existing list
        variableSetToUpdate.removeVariableDefinitions(variableSetToUpdate.getVariableDefinitions());

        // save modified existing variables set entity
        // variable definitions are systematically deleted via orphanRemoval = true
        modelVariablesSetRepository.save(variableSetToUpdate);
        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public Model addNewVariablesSetsToModel(String modelName, List<VariablesSet> variableSets) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (variableSets != null && !variableSets.isEmpty()) {
            // do merge with existing list
            List<ModelVariableSetEntity> variablesSetEntities = variableSets.stream().map(variablesSet -> new ModelVariableSetEntity(modelToUpdate, variablesSet)).collect(Collectors.toList());
            modelToUpdate.addVariablesSets(variablesSetEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model addExistingVariablesSetsToModel(String modelName, List<String> variablesSetNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (variablesSetNames != null && !variablesSetNames.isEmpty()) {
            // do merge with existing list
            List<ModelVariableSetEntity> foundVariablesSetEntities = modelVariablesSetRepository.findAllById(variablesSetNames);

            // check whether found all
            if (foundVariablesSetEntities.size() != variablesSetNames.size()) {
                List<String> foundNames = foundVariablesSetEntities.stream().map(ModelVariableSetEntity::getName).collect(Collectors.toList());
                List<String> notFoundNames = variablesSetNames.stream().filter(name -> !foundNames.contains(name)).collect(Collectors.toList());
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some variables set not found: " + notFoundNames);
            }

            modelToUpdate.addVariablesSets(foundVariablesSetEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model removeExistingVariablesSetsFromModel(String modelName, List<String> variablesSetNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (variablesSetNames != null && !variablesSetNames.isEmpty()) {
            // remove from existing list
            List<ModelVariableSetEntity> foundVariablesSetEntities = modelVariablesSetRepository.findAllById(variablesSetNames);
            modelToUpdate.removeVariablesSets(foundVariablesSetEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    public Model removeAllExistingVariablesSetsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelToUpdate(modelName, foundModelOpt);

        modelToUpdate.removeVariablesSets(modelToUpdate.getVariableSets());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public List<String> deleteVariableDefinitions(List<String> variableDefinitionNames) {
        if (variableDefinitionNames != null && !variableDefinitionNames.isEmpty()) {
            modelVariableRepository.deleteAllById(variableDefinitionNames);
        }

        return variableDefinitionNames;
    }

    @Override
    @Transactional
    public List<String> deleteVariablesSets(List<String> variablesSetNames) {

        if (variablesSetNames != null && !variablesSetNames.isEmpty()) {

            // cleanup associations
            List<ModelVariableSetEntity> foundVariablesSetEntities = modelVariablesSetRepository.findAllById(variablesSetNames);
            for (ModelVariableSetEntity variableSetEntity : foundVariablesSetEntities) {
                variableSetEntity.removeVariableDefinitions(variableSetEntity.getVariableDefinitions());
            }

            // delete entity
            List<String> foundVariablesSetNames = foundVariablesSetEntities.stream().map(ModelVariableSetEntity::getName).collect(Collectors.toList());
            modelVariablesSetRepository.deleteAllById(foundVariablesSetNames);

            return foundVariablesSetNames;
        }

        return List.of();
    }
    // --- END variable-related service methods --- //

}

