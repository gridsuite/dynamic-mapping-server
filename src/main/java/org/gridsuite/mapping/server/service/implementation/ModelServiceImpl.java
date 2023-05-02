/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.model.ModelEntity;
import org.gridsuite.mapping.server.model.ModelParameterSetEntity;
import org.gridsuite.mapping.server.model.ModelSetsGroupEntity;
import org.gridsuite.mapping.server.model.ModelVariableDefinitionEntity;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ModelVariableRepository;
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

    private final ModelRepository modelRepository;
    private final ModelVariableRepository modelVariableRepository;

    @Autowired
    public ModelServiceImpl(
            ModelRepository modelRepository,
            ModelVariableRepository modelVariableRepository
    ) {
        this.modelRepository = modelRepository;
        this.modelVariableRepository = modelVariableRepository;
    }

    @Override
    public List<ModelParameterDefinition> getParametersDefinitionsFromModelName(String modelName) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);
        if (foundModel.isPresent()) {
            Model modelToSend = new Model(foundModel.get());
            return modelToSend.getParameterDefinitions();
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
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

    @Override
    @Transactional
    public Model addNewVariableDefinitionsToModel(String modelName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = foundModel.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // do merge with the list of variable definitions
        if (variableDefinitions != null && !variableDefinitions.isEmpty()) {
            // do merge with existing list
            List<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream().map(variableDefinition -> new ModelVariableDefinitionEntity(modelToUpdate, null, variableDefinition)).collect(Collectors.toList());
            modelToUpdate.addVariableDefinitions(variableDefinitionEntities);
            // save modified existing model entity
            ModelEntity savedModelEntity = modelRepository.save(modelToUpdate);
            return new Model(savedModelEntity);
        } else {
            return new Model(modelToUpdate);
        }
    }

    @Override
    @Transactional
    public Model addExistingVariableDefinitionsToModel(String modelName, List<String> variableDefinitionNames) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = foundModel.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // do merge with the list of variable definitions
        if (variableDefinitionNames != null && !variableDefinitionNames.isEmpty()) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // check whether found all
            if (foundVariableDefinitionEntities.size() != variableDefinitionNames.size()) {
                List<String> foundNames = foundVariableDefinitionEntities.stream().map(ModelVariableDefinitionEntity::getName).collect(Collectors.toList());
                List<String> notFoundNames = variableDefinitionNames.stream().filter(name -> !foundNames.contains(name)).collect(Collectors.toList());
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some variable definition not found: " + notFoundNames);
            }

            // do merge with existing list
            modelToUpdate.addVariableDefinitions(foundVariableDefinitionEntities);

            // save modified existing model entity
            ModelEntity savedModelEntity = modelRepository.save(modelToUpdate);
            return new Model(savedModelEntity);
        } else {
            return new Model(modelToUpdate);
        }
    }

    @Override
    @Transactional
    public Model removeExistingVariableDefinitionsFromModel(String modelName, List<String> variableDefinitionNames) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = foundModel.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // do remove in the list of variable definitions
        if (variableDefinitionNames != null && !variableDefinitionNames.isEmpty()) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // remove in existing list
            modelToUpdate.removeVariableDefinitions(foundVariableDefinitionEntities);

            // save modified existing model entity
            ModelEntity savedModelEntity = modelRepository.save(modelToUpdate);
            return new Model(savedModelEntity);
        } else {
            return new Model(modelToUpdate);
        }
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
    public Model resetVariableDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = foundModel.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));

         // clear the existing list
        modelToUpdate.removeVariableDefinitions(modelToUpdate.getVariableDefinitions());

        // save modified existing model entity
        ModelEntity savedModelEntity = modelRepository.save(modelToUpdate);
        return new Model(savedModelEntity);
    }
}

