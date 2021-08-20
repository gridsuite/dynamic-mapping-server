/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
import org.gridsuite.mapping.server.model.ModelEntity;
import org.gridsuite.mapping.server.model.ModelParameterEntity;
import org.gridsuite.mapping.server.model.ModelParameterSetEntity;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Date;
import java.util.List;
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
    public List<ParametersSet> getParametersSetsFromModelName(String modelName) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);
        if (foundModel.isPresent()) {
            Model modelToSend = new Model(foundModel.get());
            return modelToSend.getSets();
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ParametersSet saveParametersSet(String modelName, ParametersSet set) {
        Optional<ModelEntity> foundModel = modelRepository.findById(modelName);
        if (foundModel.isPresent()) {
            ModelEntity modelToUpdate = foundModel.get();
            List<ModelParameterSetEntity> savedSets = modelToUpdate.getSets();
            ModelParameterSetEntity previousSet = savedSets.stream().filter(savedSet -> savedSet.getName() == set.getName()).findAny().orElse(null);
            ModelParameterSetEntity setToAdd = new ModelParameterSetEntity(modelToUpdate, set);
            if (previousSet == null) {
                set.setLastModifiedDate(new Date());
                savedSets.add(setToAdd);
            } else {
                previousSet.setParameters(set.getParameters().stream().map(modelParameter -> new ModelParameterEntity(previousSet, modelParameter)).collect(Collectors.toList()));
                previousSet.setLastModifiedDate(new Date());
            }
            modelRepository.save(modelToUpdate);
            return set;
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
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
}

