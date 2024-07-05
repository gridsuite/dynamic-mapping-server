/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.Parameter;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.ModelEntity;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.service.ParameterService;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.gridsuite.mapping.server.utils.Templater;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Service
public class ParameterServiceImpl implements ParameterService {

    public static final String MODEL_NOT_FOUND_MSG = "No model has been found with this name: ";
    public static final String MAPPING_NOT_FOUND_MSG = "No mapping has been found with this name: ";

    private final ModelRepository modelRepository;
    private final MappingRepository mappingRepository;

    public ParameterServiceImpl(
            MappingRepository mappingRepository,
            ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        this.mappingRepository = mappingRepository;
    }

    @Override
    public Parameter getParameters(String mappingName) {
        Optional<MappingEntity> foundMapping = mappingRepository.findById(mappingName);
        if (foundMapping.isPresent()) {
            String createdPar = null;
            if (foundMapping.get().isControlledParameters()) {
                // build enriched parameters sets from the mapping
                Set<InstantiatedModel> instantiatedModels = foundMapping.get().getRules().stream()
                    .map(InstantiatedModel::new)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
                instantiatedModels.addAll(foundMapping.get().getAutomata().stream()
                    .map(InstantiatedModel::new)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));

                List<List<EnrichedParametersSet>> setsLists = instantiatedModels.stream()
                    .map(instantiatedModel -> getEnrichedSetsFromInstanceModel(
                        instantiatedModel.getModel(),
                        instantiatedModel.getSetGroup()))
                    .toList();
                List<EnrichedParametersSet> sets = new ArrayList<>();
                setsLists.forEach(sets::addAll);

                // generate .par content
                createdPar = Templater.setsToPar(sets);
            }
            return new Parameter(mappingName, createdPar);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MAPPING_NOT_FOUND_MSG + mappingName);
        }
    }

    private List<EnrichedParametersSet> getEnrichedSetsFromInstanceModel(String modelName, String groupName) {
        Optional<ModelEntity> model = modelRepository.findById(modelName);
        if (model.isPresent()) {
            ParametersSetsGroup correspondingGroup = new ParametersSetsGroup(model.get().getSetsGroups().stream()
                .filter(group -> group.getName().equals(groupName))
                .findAny().orElseThrow());
            List<ParametersSet> correspondingSets = correspondingGroup.getSets();
            List<ModelParameterDefinition> correspondingDefinitions = model.get().getParameterDefinitions().stream()
                .map(parameterDefinition -> new ModelParameterDefinition(
                    parameterDefinition.getParameterDefinition(),
                    parameterDefinition.getOrigin(),
                    parameterDefinition.getOriginName()))
                .toList();

            return correspondingSets.stream().map(set -> new EnrichedParametersSet(set, correspondingDefinitions)).toList();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MODEL_NOT_FOUND_MSG + modelName);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EnrichedParametersSet {
        private String name;
        private List<EnrichedParameter> parameters;

        public EnrichedParametersSet(ParametersSet set, List<ModelParameterDefinition> definitions) {
            name = set.getName();
            parameters = definitions.stream().map(definition ->
                    new EnrichedParameter(
                            definition.getName(),
                            definition.getOrigin() == ParameterOrigin.NETWORK ? null :
                                    set.getParameters().stream()
                                            .filter(parameter -> parameter.getName().equals(definition.getName()))
                                            .findAny().orElseThrow().getValue(),
                            definition.getType(),
                            definition.getOrigin(),
                            definition.getOriginName()
                    )).toList();
        }
    }

    public record EnrichedParameter(String name, String value, ParameterType type, ParameterOrigin origin, String originName) { }

    @Getter
    @EqualsAndHashCode
    public static class InstantiatedModel {
        private final String model;
        private final String setGroup;

        public InstantiatedModel(RuleEntity rule) {
            this.model = rule.getMappedModel();
            this.setGroup = rule.getSetGroup();
        }

        public InstantiatedModel(AutomatonEntity automaton) {
            this.model = automaton.getModel();
            this.setGroup = automaton.getSetGroup();
        }
    }
}

