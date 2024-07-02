/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import lombok.*;
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
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class ParameterServiceImpl implements ParameterService {

    private final ModelRepository modelRepository;
    private final MappingRepository mappingRepository;

    public ParameterServiceImpl(
            MappingRepository mappingRepository,
            ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        this.mappingRepository = mappingRepository;
    }

    String noModelFoundErrorMessage = "No model found with this name";

    @Override
    public Parameter createFromMapping(String mappingName) {
        Optional<MappingEntity> foundMapping = mappingRepository.findById(mappingName);
        if (foundMapping.isPresent()) {
            String createdPar = null;
            if (foundMapping.get().isControlledParameters()) {
                try {
                    // build enriched parameters sets from the mapping
                    Set<InstantiatedModel> instantiatedModels = foundMapping.get().getRules().stream()
                            .map(InstantiatedModel::new)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    instantiatedModels.addAll(foundMapping.get().getAutomata().stream()
                            .map(InstantiatedModel::new)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));

                    List<List<EnrichedParametersSet>> setsLists = instantiatedModels.stream()
                            .map(instantiatedModel -> getEnrichedSetsFromInstanceModel(instantiatedModel.getModel(), instantiatedModel.getSetGroup()))
                            .collect(Collectors.toList());
                    List<EnrichedParametersSet> sets = new ArrayList<>();
                    setsLists.forEach(sets::addAll);

                    // generate .par content
                    createdPar = Templater.setsToPar(sets);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter sets");
                }
            }
            return new Parameter(mappingName, createdPar);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

    private List<EnrichedParametersSet> getEnrichedSetsFromInstanceModel(String modelName, String groupName) {
        Optional<ModelEntity> model = modelRepository.findById(modelName);
        if (model.isPresent()) {
            ParametersSetsGroup correspondingGroup = new ParametersSetsGroup(model.get().getSetsGroups().stream().filter(group -> group.getName().equals(groupName)).findAny().orElseThrow());
            List<ParametersSet> correspondingSets = correspondingGroup.getSets();
            List<ModelParameterDefinition> correspondingDefinitions = model.get().getParameterDefinitions().stream()
                    .map(parameterDefinition -> new ModelParameterDefinition(parameterDefinition.getParameterDefinition(), parameterDefinition.getOrigin(), parameterDefinition.getOriginName()))
                    .toList();

            return correspondingSets.stream().map(set -> new EnrichedParametersSet(set, correspondingDefinitions)).toList();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, noModelFoundErrorMessage);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class EnrichedParametersSet {
        private String name;
        private List<EnrichedParameter> parameters;

        public EnrichedParametersSet(ParametersSet set, List<ModelParameterDefinition> definitions) {
            name = set.getName();
            parameters = definitions.stream().map(definition ->
                    new EnrichedParameter(
                            definition.getName(),
                            definition.getOrigin() == ParameterOrigin.NETWORK ? null : set.getParameters().stream().filter(parameter -> parameter.getName().equals(definition.getName())).findAny().orElseThrow().getValue(),
                            definition.getType(),
                            definition.getOrigin(),
                            definition.getOriginName()
                    )).toList();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class EnrichedParameter {
        private String name;
        private String value;
        private ParameterType type;
        private ParameterOrigin origin;
        private String originName;


    }

    @Getter
    @EqualsAndHashCode
    public class InstantiatedModel {
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

