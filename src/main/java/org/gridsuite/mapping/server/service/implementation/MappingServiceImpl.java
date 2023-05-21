/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.automata.plugins.AutomatonPluggableTypesPlugin;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.service.MappingService;
import org.gridsuite.mapping.server.utils.Methods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.mapping.server.MappingConstants.DEFAULT_MAPPING_NAME;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class MappingServiceImpl implements MappingService {

    private final ModelRepository modelRepository;
    private final MappingRepository mappingRepository;
    private final AutomatonPluggableTypesPlugin automatonPluggableTypesPlugin;

    @Autowired
    public MappingServiceImpl(
            MappingRepository mappingRepository,
            ModelRepository modelRepository,
            AutomatonPluggableTypesPlugin automatonPluggableTypesPlugin
    ) {
        this.modelRepository = modelRepository;
        this.mappingRepository = mappingRepository;
        this.automatonPluggableTypesPlugin = automatonPluggableTypesPlugin;
    }

    @Override
    public List<InputMapping> getMappingList() {
        List<MappingEntity> mappingEntities = mappingRepository.findAll();

        return mappingEntities.stream().map(entity -> new InputMapping(entity, automatonPluggableTypesPlugin)).collect(Collectors.toList());
    }

    @Override
    public InputMapping createMapping(String mappingName, InputMapping mapping) {
        MappingEntity mappingToSave = mapping.convertMappingToEntity();
        mappingToSave.markNotNew();
        if (mappingToSave.isControlledParameters()) {
            try {
                List<String[]> instantiatedModels = mappingToSave.getRules().stream().map(ruleEntity ->
                        new String[]{
                                ruleEntity.getMappedModel(), ruleEntity.getSetGroup()
                        }
                ).collect(Collectors.toList());
                for (String[] instantiatedModel : instantiatedModels) {
                    ParametersSetsGroup parametersSetsGroup = Methods.getSetsGroupFromModel(instantiatedModel[0], instantiatedModel[1], modelRepository);
                    if (parametersSetsGroup.getSets().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No sets associated to the group");
                    }
                }

            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parameter sets not found", e);
            }
        }
        mappingRepository.save(mappingToSave);
        return mapping;
    }

    @Override
    public String deleteMapping(String mappingName) {
        mappingRepository.deleteById(mappingName);
        return mappingName;
    }

    static String conflictMappingErrorMessage = "A Mapping with this name already exists";

    @Override
    public RenameObject renameMapping(String oldName, String newName) {
        Optional<MappingEntity> mappingToRename = mappingRepository.findById(oldName);
        if (mappingToRename.isPresent()) {
            MappingEntity mappingToSave = new MappingEntity(newName, mappingToRename.get());
            try {
                mappingRepository.deleteById(oldName);
                mappingRepository.save(mappingToSave);
                return new RenameObject(oldName, newName);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMappingErrorMessage, ex);
            }
        } else if (oldName.equals(DEFAULT_MAPPING_NAME)) {
            // In case of naming of new mapping, save it to db.
            try {
                mappingRepository.save(new MappingEntity(newName, new ArrayList<>(), new ArrayList<>(), false));
                return new RenameObject(DEFAULT_MAPPING_NAME, newName);

            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMappingErrorMessage, ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

    @Override
    public InputMapping copyMapping(String originalName, String copyName) {
        Optional<MappingEntity> mappingToCopy = mappingRepository.findById(originalName);
        if (mappingToCopy.isPresent()) {
            MappingEntity copiedMapping = new MappingEntity(copyName, mappingToCopy.get());
            try {
                mappingRepository.save(copiedMapping);
                return new InputMapping(copiedMapping, automatonPluggableTypesPlugin);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMappingErrorMessage, ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

    @Override
    public List<Model> getMappedModelsList(String mappingName) {
        Optional<MappingEntity> mappingEntityOpt = mappingRepository.findById(mappingName);
        MappingEntity mapping = mappingEntityOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name : " + mappingName));

        // models used by rule
        List<RuleEntity> ruleEntities = mapping.getRules();
        Set<String> ruleModelNames = ruleEntities.stream().map(RuleEntity::getMappedModel).collect(Collectors.toSet());

        // model used by automaton
        List<AutomatonEntity> automatonEntities = mapping.getAutomata();
        Set<String> automatonModelNames = automatonEntities.stream().map(AutomatonEntity::getModel).collect(Collectors.toSet());

        // concat models used by rule and models used by automaton
        Set<String> mappedModelNames = Stream.concat(ruleModelNames.stream(), automatonModelNames.stream())
                .collect(Collectors.toSet());

        // get model by name from db and convert to dtos
        List<Model> mappedModels = mappedModelNames.stream()
                .map(mappedModelName -> modelRepository.findById(mappedModelName)
                        .map(Model::new)
                        .orElse(null))
                .filter(Objects::nonNull) // only keep found models
                .collect(Collectors.toList());

        return mappedModels;
    }

}

