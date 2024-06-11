/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.DynamicMappingException;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.RuleRepository;
import org.gridsuite.mapping.server.service.MappingService;
import org.gridsuite.mapping.server.service.client.filter.FilterClient;
import org.gridsuite.mapping.server.utils.Methods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.mapping.server.DynamicMappingException.Type.MAPPING_NAME_NOT_PROVIDED;
import static org.gridsuite.mapping.server.MappingConstants.DEFAULT_MAPPING_NAME;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class MappingServiceImpl implements MappingService {

    public static final String CONFLICT_MAPPING_ERROR_MESSAGE = "A mapping already exists with name: ";
    public static final String MAPPING_NOT_FOUND_ERROR_MESSAGE = "Mapping not found with name: ";

    private final ModelRepository modelRepository;
    private final MappingRepository mappingRepository;
    private final RuleRepository ruleRepository;
    private final FilterClient filterClient;

    @Autowired
    public MappingServiceImpl(
            MappingRepository mappingRepository,
            ModelRepository modelRepository,
            RuleRepository ruleRepository,
            FilterClient filterClient
    ) {
        this.modelRepository = modelRepository;
        this.mappingRepository = mappingRepository;
        this.ruleRepository = ruleRepository;
        this.filterClient = filterClient;
    }

    @Transactional(readOnly = true)
    @Override
    public List<InputMapping> getMappingList() {
        List<MappingEntity> mappingEntities = mappingRepository.findAll();

        List<InputMapping> mappings = mappingEntities.stream().map(InputMapping::new).toList();

        if (CollectionUtils.isNotEmpty(mappings)) {
            // get all filter uuids to GET from filter-server
            List<UUID> filterUuids = mappings.stream()
                    .flatMap(mapping -> mapping.getRules().stream())
                    .map(rule -> Optional.ofNullable(rule.getFilterUuid()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            if (CollectionUtils.isNotEmpty(filterUuids)) {
                List<ExpertFilter> filters = filterClient.getFilters(filterUuids);
                Map<UUID, ExpertFilter> filterByUuidMap = filters.stream().collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));

                // enrich rules with retrieved filter
                mappings.stream().flatMap(mapping -> mapping.getRules().stream()).forEach(rule -> {
                    ExpertFilter expertFilter = filterByUuidMap.get(rule.getFilterUuid());
                    rule.setFilter(expertFilter);
                });
            }
        }

        return mappings;
    }

    @Transactional(readOnly = true)
    @Override
    public InputMapping getMapping(String mappingName) {
        Optional<MappingEntity> mappingEntityOpt = mappingRepository.findById(mappingName);
        MappingEntity mappingEntity = mappingEntityOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MAPPING_NOT_FOUND_ERROR_MESSAGE + mappingName));
        InputMapping mapping = new InputMapping(mappingEntity);

        // get all filter uuids to GET from filter-server
        List<UUID> filterUuids = mapping.getRules().stream()
                .map(rule -> Optional.ofNullable(rule.getFilterUuid()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (CollectionUtils.isNotEmpty(filterUuids)) {
            List<ExpertFilter> filters = filterClient.getFilters(filterUuids);
            Map<UUID, ExpertFilter> filterByUuidMap = filters.stream().collect(Collectors.toMap(ExpertFilter::getId, filter -> filter));

            // enrich rules with retrieved filter
            mapping.getRules().forEach(rule -> {
                ExpertFilter expertFilter = filterByUuidMap.get(rule.getFilterUuid());
                rule.setFilter(expertFilter);
            });
        }

        return mapping;
    }

    @Override
    public InputMapping createMapping(String mappingName, InputMapping mapping) {
        if (!StringUtils.isBlank(mappingName)) {
            mapping.setName(mappingName);
        }

        if (StringUtils.isBlank(mapping.getName())) {
            throw new DynamicMappingException(MAPPING_NAME_NOT_PROVIDED, "Mapping name not provided");
        }

        // get all filterUuids used previously in the mapping to infer to update/create/delete filters
        List<UUID> filterUuids = ruleRepository.findFilterUuidsByMappingName(mappingName);

        // IMPORTANT: new filter is enriched with new uuid while converting the whole mapping in cascade
        // So must do converting before persisting filter in filter-server to ensure that new uuid is provided
        MappingEntity mappingToSave = mapping.convertMappingToEntity();

        // --- update or create filters appeared in rules in remote filter-server --- //
        Map<Boolean, Map<UUID, ExpertFilter>> filtersToUpdateOrCreate = mapping.getRules().stream()
                .map(rule -> Optional.ofNullable(rule.getFilter()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.partitioningBy(
                    filter -> filterUuids.contains(filter.getId()),
                    Collectors.toMap(ExpertFilter::getId, filter -> filter)
                ));

        // filters to update
        Map<UUID, ExpertFilter> filtersToUpdateMap = filtersToUpdateOrCreate.get(Boolean.TRUE);

        // filter to create
        Map<UUID, ExpertFilter> filtersToCreateMap = filtersToUpdateOrCreate.get(Boolean.FALSE);

        // filter to delete
        List<UUID> filterUuidsToDelete = filterUuids.stream().filter(uuid -> !filtersToUpdateMap.containsKey(uuid)).toList();

        filterClient.updateFilters(filtersToUpdateMap);
        filterClient.createFilters(filtersToCreateMap);

        // --- persist in cascade the mapping in local database --- //
        mappingToSave.markNotNew();
        if (mappingToSave.isControlledParameters()) {
            List<String[]> instantiatedModels = mappingToSave.getRules().stream().map(ruleEntity ->
                    new String[]{
                            ruleEntity.getMappedModel(), ruleEntity.getSetGroup()
                    }
            ).toList();
            for (String[] instantiatedModel : instantiatedModels) {
                ParametersSetsGroup parametersSetsGroup = Methods.getSetsGroupFromModel(instantiatedModel[0], instantiatedModel[1], modelRepository);
                if (parametersSetsGroup.getSets().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No sets associated to the group of model " +
                        instantiatedModel[0] + ": " + instantiatedModel[1]);
                }
            }
        }
        mappingRepository.save(mappingToSave);

        // --- clean filters in filter-server --- //
        if (CollectionUtils.isNotEmpty(filterUuidsToDelete)) {
            filterClient.deleteFilters(filterUuidsToDelete);
        }

        return mapping;
    }

    @Override
    public String deleteMapping(String mappingName) {
        // get all filterUuids used in the mapping to delete if exists
        List<UUID> filterUuids = ruleRepository.findFilterUuidsByMappingName(mappingName);

        // --- delete filters in filter-server --- //
        if (CollectionUtils.isNotEmpty(filterUuids)) {
            filterClient.deleteFilters(filterUuids);
        }

        // --- delete the whole mapping in local db --- //
        mappingRepository.deleteById(mappingName);
        return mappingName;
    }

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
                throw new ResponseStatusException(HttpStatus.CONFLICT, CONFLICT_MAPPING_ERROR_MESSAGE + newName, ex);
            }
        } else if (oldName.equals(DEFAULT_MAPPING_NAME)) {
            // In case of naming of new mapping, save it to db.
            try {
                mappingRepository.save(new MappingEntity(newName, new ArrayList<>(), new ArrayList<>(), false));
                return new RenameObject(DEFAULT_MAPPING_NAME, newName);

            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, CONFLICT_MAPPING_ERROR_MESSAGE + newName, ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MAPPING_NOT_FOUND_ERROR_MESSAGE + oldName);
        }
    }

    @Override
    public InputMapping copyMapping(String originalName, String copyName) {
        Optional<MappingEntity> mappingToCopyOpt = mappingRepository.findById(originalName);
        MappingEntity mappingToCopy = mappingToCopyOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MAPPING_NOT_FOUND_ERROR_MESSAGE + originalName));

        MappingEntity copiedMapping = new MappingEntity(copyName, mappingToCopy);
        try {
            // --- duplicate filters in filter-server--- //
            // get all filter uuids that needs to duplicate its corresponding filter
            List<UUID> filterUuids = copiedMapping.getRules().stream()
                .map(RuleEntity::getFilterUuid)
                .filter(Objects::nonNull)
                .toList();

            if (CollectionUtils.isNotEmpty(filterUuids)) {
                // call filter-server API to duplicate filter
                Map<UUID, UUID> uuidsMap = filterClient.duplicateFilters(filterUuids);

                // replace the old by the new uuid for rule entities
                copiedMapping.getRules().stream()
                        .filter(rule -> rule.getFilterUuid() != null)
                        .forEach(rule -> rule.setFilterUuid(uuidsMap.get(rule.getFilterUuid())));
            }

            // --- persist in cascade the mapping in local database --- //
            mappingRepository.save(copiedMapping);
            return new InputMapping(copiedMapping);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, CONFLICT_MAPPING_ERROR_MESSAGE + copyName, ex);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Model> getMappedModelsList(String mappingName) {
        Optional<MappingEntity> mappingEntityOpt = mappingRepository.findById(mappingName);
        MappingEntity mapping = mappingEntityOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MAPPING_NOT_FOUND_ERROR_MESSAGE + mappingName));

        // models used by rule
        List<RuleEntity> ruleEntities = mapping.getRules();
        Set<String> ruleModelNames = ruleEntities.stream().map(RuleEntity::getMappedModel).collect(Collectors.toSet());

        // model used by automaton
        List<AutomatonEntity> automatonEntities = mapping.getAutomata();
        Set<String> automatonModelNames = automatonEntities.stream().map(AutomatonEntity::getModel).collect(Collectors.toSet());

        // concat models used by rule and models used by automaton
        Set<String> mappedModelNames = Stream.concat(ruleModelNames.stream(), automatonModelNames.stream())
                .collect(Collectors.toSet());

        // get model by name from db, concat to default models and convert to dtos
        List<Model> mappedModels = Stream.concat(modelRepository.findAllById(mappedModelNames).stream(),
                        modelRepository.findAllByDefaultModelTrue().stream())
                .map(Model::new).toList();

        return mappedModels;
    }

}

