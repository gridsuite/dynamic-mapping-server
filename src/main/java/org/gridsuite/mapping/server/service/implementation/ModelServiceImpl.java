/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.model.ModelEntity;
import org.gridsuite.mapping.server.model.ModelSetsGroupEntity;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.service.ModelService;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class ModelServiceImpl implements ModelService {

    private final ModelRepository modelRepository;

    @Autowired
    public ModelServiceImpl(
            ModelRepository modelRepository
    ) {
        this.modelRepository = modelRepository;
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
                previousGroup.setSets(groupToAdd.getSets());
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
}

