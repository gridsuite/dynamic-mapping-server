/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.*;
import org.gridsuite.mapping.server.service.ModelService;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class ModelServiceImpl implements ModelService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceImpl.class);

    public static final String AUTOMATON_DIR_SCAN_PATTERN = "classpath*:automaton/*.json";
    public static final String INVALID_AUTOMATON_JSON_FORMAT = "Invalid automaton json format";
    public static final String CAN_NOT_READ_AUTOMATON_FILE = "Can not read automaton file";
    public static final String CAN_NOT_READ_AUTOMATON_DIRECTORY = "Can not read automaton directory";

    public static final String MODEL_NOT_FOUND = "Model not found: ";
    public static final String VARIABLES_SET_NOT_FOUND = "Variables set not found: ";
    public static final String SETS_GROUP_NOT_FOUND = "Sets group not found: ";

    @Value("${ex-resources.automaton:}")
    String exResourcesAutomaton;

    private final ResourcePatternResolver resourcePatternResolver;
    private final ObjectMapper objectMapper;
    private final ModelRepository modelRepository;
    private final ModelSetsGroupRepository modelSetsGroupRepository;
    private final ModelParameterSetRepository modelParameterSetRepository;
    private final ModelParameterRepository modelParameterRepository;
    private final ModelParameterDefinitionRepository modelParameterDefinitionRepository;
    private final ModelVariableRepository modelVariableRepository;
    private final ModelVariablesSetRepository modelVariablesSetRepository;

    @Autowired
    public ModelServiceImpl(
            ResourcePatternResolver resourcePatternResolver,
            ObjectMapper objectMapper,
            ModelRepository modelRepository,
            ModelSetsGroupRepository modelSetsGroupRepository,
            ModelParameterSetRepository modelParameterSetRepository,
            ModelParameterRepository modelParameterRepository,
            ModelParameterDefinitionRepository modelParameterDefinitionRepository,
            ModelVariableRepository modelVariableRepository,
            ModelVariablesSetRepository modelVariablesSetRepository
    ) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.objectMapper = objectMapper;
        this.modelRepository = modelRepository;
        this.modelSetsGroupRepository = modelSetsGroupRepository;
        this.modelParameterSetRepository = modelParameterSetRepository;
        this.modelParameterRepository = modelParameterRepository;
        this.modelParameterDefinitionRepository = modelParameterDefinitionRepository;
        this.modelVariableRepository = modelVariableRepository;
        this.modelVariablesSetRepository = modelVariablesSetRepository;
    }

    private ModelEntity getModelFromOptional(String modelInfo, Optional<ModelEntity> foundModelOpt) {
        return foundModelOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, MODEL_NOT_FOUND + modelInfo));
    }

    private ModelVariableSetEntity getVariableSetFromOptional(String variableSetInfo, Optional<ModelVariableSetEntity> foundVariableSetOpt) {
        return foundVariableSetOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, VARIABLES_SET_NOT_FOUND + variableSetInfo));
    }

    private ModelSetsGroupEntity getSetsGroupFromOptional(String setsGroupInfo, Optional<ModelSetsGroupEntity> foundSetsGroupOpt) {
        return foundSetsGroupOpt.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, SETS_GROUP_NOT_FOUND + setsGroupInfo));
    }

    private ArrayNode readAutomatonFromResources(Resource[] resources) {
        ArrayNode automatonArrayNode = objectMapper.createArrayNode();
        for (Resource resource : resources) {
            try (InputStream is = resource.getInputStream()) {
                JsonNode jsonNode = objectMapper.readTree(is);
                automatonArrayNode.addAll(jsonNode.isArray() ? IterableUtils.toList(jsonNode) : Arrays.asList(jsonNode));
            } catch (StreamReadException e) {
                LOGGER.error(INVALID_AUTOMATON_JSON_FORMAT, e);
            } catch (IOException e) {
                LOGGER.error(CAN_NOT_READ_AUTOMATON_FILE, e);
            }
        }
        return automatonArrayNode;
    }

    private ArrayNode readAutomatonFromFilePath(List<Path> jsonFilePaths) {
        ArrayNode automatonArrayNode = objectMapper.createArrayNode();
        for (Path jsonFilePath : jsonFilePaths) {
            try (InputStream is = Files.newInputStream(jsonFilePath)) {
                JsonNode jsonNode = objectMapper.readTree(is);
                automatonArrayNode.addAll(jsonNode.isArray() ? IterableUtils.toList(jsonNode) : Arrays.asList(jsonNode));
            } catch (StreamReadException e) {
                LOGGER.error(INVALID_AUTOMATON_JSON_FORMAT, e);
            } catch (IOException e) {
                LOGGER.error(CAN_NOT_READ_AUTOMATON_FILE, e);
            }
        }
        return automatonArrayNode;
    }

    @Override
    public String getAutomatonDefinitions() {

        // read json file from resources into a Json ArrayNode, support both single object and array object json
        ArrayNode automatonArrayNode = objectMapper.createArrayNode();

        // read automation definitions from Internal resources
        Resource[] resources = new Resource[0];
        try {
            resources = resourcePatternResolver.getResources(AUTOMATON_DIR_SCAN_PATTERN);
        } catch (IOException e) {
            LOGGER.error(CAN_NOT_READ_AUTOMATON_DIRECTORY, e);
        }

        if (!ArrayUtils.isEmpty(resources)) {
            automatonArrayNode.addAll(readAutomatonFromResources(resources));
        }

        // read automation definitions from External resources
        List<Path> jsonFilePaths = new ArrayList<>();
        try (Stream<Path> streamPaths = !StringUtils.isEmpty(exResourcesAutomaton) ?
                Files.list(Paths.get(exResourcesAutomaton)) :
                Stream.empty()
        ) {
            jsonFilePaths.addAll(streamPaths.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.getFileName().toString().endsWith("json")).toList());

        } catch (IOException e) {
            // do nothing, external resources is optional
        }

        automatonArrayNode.addAll(readAutomatonFromFilePath(jsonFilePaths));

        return automatonArrayNode.toPrettyString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimpleModel> getModels() {
        return modelRepository.findAll().stream().map(modelEntity -> new SimpleModel(new Model(modelEntity))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelParameter> getParameters() {
        return modelParameterRepository.findAll().stream().map(ModelParameter::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametersSetsGroup> getParametersSetsGroups() {
        return modelSetsGroupRepository.findAll().stream().map(ParametersSetsGroup::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametersSet> getParametersSets() {
        return modelParameterSetRepository.findAll().stream().map(ParametersSet::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametersSet> getSetsFromGroup(String modelName, String groupName, SetGroupType groupType) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Optional<ModelSetsGroupEntity> modelSetsGroupOpt = modelEntity.getSetsGroups().stream()
                .filter(setGroup -> StringUtils.equals(setGroup.getName(), groupName) && setGroup.getType() == groupType)
                .findAny();
        ModelSetsGroupEntity setsGroup = getSetsGroupFromOptional("[" + groupName + "," + groupType.name() + "]", modelSetsGroupOpt);

        return setsGroup.getSets().stream().map(ParametersSet::new).toList();
    }

    @Override
    @Transactional
    public ParametersSetsGroup saveParametersSetsGroup(String modelName, ParametersSetsGroup setsGroup, Boolean strict) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        List<ModelSetsGroupEntity> savedGroups = modelToUpdate.getSetsGroups();
        ModelSetsGroupEntity previousGroup = savedGroups.stream().filter(savedGroup -> savedGroup.getName().equals(setsGroup.getName())).findAny().orElse(null);
        ModelSetsGroupEntity groupToAdd = new ModelSetsGroupEntity(modelToUpdate, setsGroup);
        groupToAdd.getSets().forEach(set -> set.setLastModifiedDate(new Date()));

        if (previousGroup == null) {
            savedGroups.add(groupToAdd);
        } else {
            // merge sets by name
            groupToAdd.getSets().forEach(setToAdd ->
                // if set's name is existing in the group, consider the setToAdd as an update version by setting the same id
                previousGroup.getSets().stream()
                    .filter(set -> set.getName().equals(setToAdd.getName()))
                    .findAny()
                    .ifPresent(existingSet -> {
                        setToAdd.setId(existingSet.getId());
                        // replace the whole old set by the new set
                        previousGroup.removeParameterSet(existingSet);
                        previousGroup.addParameterSet(setToAdd);
                    })
            );
        }

        if (new Model(modelToUpdate).isParameterSetGroupValid(setsGroup.getName(), strict)) {
            modelRepository.save(modelToUpdate);
            setsGroup.setId(groupToAdd.getId()); // round-trip id
            return setsGroup;
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public Model saveModel(Model model) {
        ModelEntity modelEntity = new ModelEntity(model);

        // --- Process reusable entities --- //
        // *** parameter definitions at model level *** //
        processParameterDefinitions(modelEntity);

        // *** variable definitions *** //
        // A map to store managed/loaded variables which can be shared with other models or within the model
        Map<String, ModelVariableDefinitionEntity> sharedVariables = new HashMap<>();

        // variables at the model level
        processModelLevelVariableDefinitions(modelEntity, sharedVariables);
        // variables at the variable-set level
        processVariableSetLevelDefinitions(modelEntity, sharedVariables);

        // rely on Hibernate to save
        modelRepository.save(modelEntity);
        return model;
    }

    /**
     * Process and merge parameter definitions at the model level with existing definitions in the database.
     * Reuses existing parameter definitions where possible to maintain referential integrity.
     */
    private void processParameterDefinitions(ModelEntity modelEntity) {
        List<ModelModelParameterDefinitionEntity> parameterDefinitionsInModel = modelEntity.getParameterDefinitions();
        if (CollectionUtils.isEmpty(parameterDefinitionsInModel)) {
            return;
        }

        List<String> parameterNames = parameterDefinitionsInModel.stream()
                .map(ModelModelParameterDefinitionEntity::getParameterDefinition)
                .map(ModelParameterDefinitionEntity::getName)
                .toList();
        List<ModelParameterDefinitionEntity> existingParameterDefinitions =
                modelParameterDefinitionRepository.findAllByNameIn(parameterNames);

        if (CollectionUtils.isEmpty(existingParameterDefinitions)) {
            return;
        }

        // indexing by parameter's name
        Map<String, ModelParameterDefinitionEntity> existingParameterDefinitionsMap = existingParameterDefinitions.stream()
                .collect(Collectors.toMap(ModelParameterDefinitionEntity::getName, Function.identity()));

        // perform merge
        for (ModelModelParameterDefinitionEntity parameterDefinitionLink : parameterDefinitionsInModel) {
            String parameterName = parameterDefinitionLink.getParameterDefinition().getName();
            if (existingParameterDefinitionsMap.containsKey(parameterName)) {
                parameterDefinitionLink.setParameterDefinition(existingParameterDefinitionsMap.get(parameterName));
            }
        }
    }

    /**
     * Process and merge variable definitions at the model level with existing definitions in the database.
     * Populates the sharedVariables map with managed/loaded variables that can be shared.
     */
    private void processModelLevelVariableDefinitions(ModelEntity modelEntity,
                                                      Map<String, ModelVariableDefinitionEntity> sharedVariables) {
        Set<ModelVariableDefinitionEntity> variableDefinitionsInModel = modelEntity.getVariableDefinitions();
        if (CollectionUtils.isEmpty(variableDefinitionsInModel)) {
            return;
        }

        List<String> variableNames = variableDefinitionsInModel.stream()
                .map(ModelVariableDefinitionEntity::getName)
                .toList();
        List<ModelVariableDefinitionEntity> existingVariables =
                modelVariableRepository.findAllByNameIn(variableNames);

        // init the shared variables map with loaded variables
        existingVariables.forEach(variable -> sharedVariables.put(variable.getName(), variable));

        Set<ModelVariableDefinitionEntity> mergedVariables = new LinkedHashSet<>();
        // perform merge
        for (ModelVariableDefinitionEntity variable : variableDefinitionsInModel) {
            if (sharedVariables.containsKey(variable.getName())) {
                mergedVariables.add(sharedVariables.get(variable.getName()));
            } else {
                mergedVariables.add(variable);
                sharedVariables.put(variable.getName(), variable);
            }
        }
        modelEntity.setVariableDefinitions(mergedVariables);
    }

    /**
     * Process and merge variable sets and their variable definitions with existing entities in the database.
     * Uses the sharedVariables map to ensure variable definitions are properly reused across sets.
     */
    private void processVariableSetLevelDefinitions(ModelEntity modelEntity,
                                                    Map<String, ModelVariableDefinitionEntity> sharedVariables) {
        Set<ModelVariableSetEntity> variableSets = modelEntity.getVariableSets();
        if (CollectionUtils.isEmpty(variableSets)) {
            return;
        }

        List<String> variableSetNames = variableSets.stream()
                .map(ModelVariableSetEntity::getName)
                .toList();
        List<ModelVariableSetEntity> existingVariableSets =
                modelVariablesSetRepository.findAllByNameIn(variableSetNames);

        // indexing by variable set's name
        Map<String, ModelVariableSetEntity> existingVariableSetMap = existingVariableSets.stream()
                .collect(Collectors.toMap(ModelVariableSetEntity::getName, Function.identity()));

        // Load variables not yet in the shared variables map
        loadReferencedVariablesBySetIntoSharedMap(variableSets, sharedVariables);

        // Merge variable sets and variables simultaneously
        Set<ModelVariableSetEntity> mergedVariableSets = mergeVariableSets(
                variableSets, existingVariableSetMap, sharedVariables, modelEntity);

        modelEntity.setVariableSets(mergedVariableSets);
    }

    /**
     * Load variable definitions that are referenced in variable sets but not yet in the shared variables map.
     */
    private void loadReferencedVariablesBySetIntoSharedMap(Set<ModelVariableSetEntity> variableSets,
                                                           Map<String, ModelVariableDefinitionEntity> sharedVariables) {
        // Collect all variable names that are in variable sets
        List<String> allVariableNamesInSets = variableSets.stream()
                .flatMap(variableSet -> variableSet.getVariableDefinitions().stream())
                .map(ModelVariableDefinitionEntity::getName)
                .toList();

        // load variables which not yet in the shared variables map
        List<String> variableNamesToLoad = allVariableNamesInSets.stream()
                .filter(name -> !sharedVariables.containsKey(name))
                .toList();

        if (CollectionUtils.isEmpty(variableNamesToLoad)) {
            return;
        }

        List<ModelVariableDefinitionEntity> existingVariablesInSet =
                modelVariableRepository.findAllByNameIn(variableNamesToLoad);
        // add new loaded variables to shared variables map
        existingVariablesInSet.forEach(variable -> sharedVariables.put(variable.getName(), variable));
    }

    /**
     * Merge variable sets with existing entities, reusing existing sets and variables where possible.
     */
    private Set<ModelVariableSetEntity> mergeVariableSets(Set<ModelVariableSetEntity> variableSets,
                                                          Map<String, ModelVariableSetEntity> existingVariableSetMap,
                                                          Map<String, ModelVariableDefinitionEntity> sharedVariables,
                                                          ModelEntity modelEntity) {
        Set<ModelVariableSetEntity> mergedVariableSets = new LinkedHashSet<>();

        for (ModelVariableSetEntity variableSet : variableSets) {
            ModelVariableSetEntity setToMerge = variableSet;

            if (existingVariableSetMap.containsKey(variableSet.getName())) {
                // Reuse the existing variable set with existing variables in the set
                setToMerge = existingVariableSetMap.get(variableSet.getName());
                if (setToMerge.getModels() == null) {
                    setToMerge.setModels(new LinkedHashSet<>());
                }
                setToMerge.getModels().add(modelEntity);
            } else {
                // For a new set, must merge with managed variables
                Set<ModelVariableDefinitionEntity> mergedVariableInSet =
                        mergeVariablesInSet(setToMerge, sharedVariables);
                setToMerge.setVariableDefinitions(mergedVariableInSet);
            }

            mergedVariableSets.add(setToMerge);
        }

        return mergedVariableSets;
    }

    /**
     * Merge variable definitions within a variable set, reusing existing variables from the shared map.
     */
    private Set<ModelVariableDefinitionEntity> mergeVariablesInSet(ModelVariableSetEntity variableSet,
                                                                   Map<String, ModelVariableDefinitionEntity> sharedVariables) {
        Set<ModelVariableDefinitionEntity> mergedVariableInSet = new LinkedHashSet<>();

        for (ModelVariableDefinitionEntity variable : variableSet.getVariableDefinitions()) {
            ModelVariableDefinitionEntity variableToMerge = variable;

            if (sharedVariables.containsKey(variable.getName())) {
                variableToMerge = sharedVariables.get(variable.getName());
                if (variableToMerge.getVariablesSets() == null) {
                    variableToMerge.setVariablesSets(new LinkedHashSet<>());
                }
                variableToMerge.getVariablesSets().add(variableSet);
            } else {
                sharedVariables.put(variable.getName(), variable);
            }

            mergedVariableInSet.add(variableToMerge);
        }

        return mergedVariableInSet;
    }

    @Override
    @Transactional
    public List<String> deleteModels(List<String> modelNames) {
        if (!CollectionUtils.isEmpty(modelNames)) {
            List<ModelEntity> modelEntities = modelRepository.findAllByModelNameIn(modelNames);
            List<ModelParameterDefinitionEntity> allParameterDefinitions = new ArrayList<>();
            Set<ModelVariableDefinitionEntity> allVariableDefinitions = new HashSet<>();
            Set<ModelVariableSetEntity> allVariableSets = new HashSet<>();

            modelEntities.forEach(modelEntity -> {

                List<ModelParameterDefinitionEntity> parameterDefinitions = modelEntity.getParameterDefinitions().stream()
                        .map(ModelModelParameterDefinitionEntity::getParameterDefinition).toList();
                modelEntity.removeAllParameterDefinition(parameterDefinitions);
                allParameterDefinitions.addAll(parameterDefinitions);

                List<ModelVariableDefinitionEntity> variableDefinitions = modelEntity.getVariableDefinitions().stream().toList();
                modelEntity.removeAllVariableDefinition(variableDefinitions);
                allVariableDefinitions.addAll(variableDefinitions);

                List<ModelVariableSetEntity> variableSets = modelEntity.getVariableSets().stream().toList();
                modelEntity.removeAllVariablesSet(variableSets);
                allVariableSets.addAll(variableSets);
            });

            // --- Perform delete cascade manually --- //
            // delete model first
            modelRepository.deleteAllByModelNameIn(modelNames);

            // delete all parameter definitions which are not referenced by any model
            List<ModelParameterDefinitionEntity> toDeleteParameterDefinitions = allParameterDefinitions.stream().filter(elem -> elem.getModels().isEmpty()).toList();
            if (!CollectionUtils.isEmpty(toDeleteParameterDefinitions)) {
                modelParameterDefinitionRepository.deleteAllByNameIn(toDeleteParameterDefinitions.stream()
                        .map(ModelParameterDefinitionEntity::getName).toList());
            }

            // delete all variable sets which are not referenced by any model
            List<ModelVariableSetEntity> toDeleteVariableSets = allVariableSets.stream().filter(elem -> elem.getModels().isEmpty()).toList();
            if (!CollectionUtils.isEmpty(toDeleteVariableSets)) {
                toDeleteVariableSets.forEach(variableSetEntity -> {
                    List<ModelVariableDefinitionEntity> variableDefinitionsInSet = variableSetEntity.getVariableDefinitions().stream().toList();
                    variableSetEntity.removeAllVariableDefinition(variableDefinitionsInSet);
                    allVariableDefinitions.addAll(variableDefinitionsInSet);
                });
                modelVariablesSetRepository.deleteAllByNameIn(toDeleteVariableSets.stream()
                        .map(ModelVariableSetEntity::getName).toList());
            }

            // delete all variable definitions which are not referenced neither model nor variable set
            List<ModelVariableDefinitionEntity> toDeleteVariableDefinitions = allVariableDefinitions.stream()
                    .filter(elem -> elem.getModels().isEmpty() && elem.getVariablesSets().isEmpty()).toList();
            if (!CollectionUtils.isEmpty(toDeleteVariableDefinitions)) {
                modelVariableRepository.deleteAllByNameIn(toDeleteVariableDefinitions.stream()
                        .map(ModelVariableDefinitionEntity::getName).toList());
            }
        }

        return modelNames;
    }

    @Override
    @Transactional
    public ParametersSetsGroup deleteSet(String modelName, String groupName, SetGroupType groupType, String setName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        Optional<ModelSetsGroupEntity> modelSetsGroupOpt = modelToUpdate.getSetsGroups().stream()
                .filter(setGroup -> StringUtils.equals(setGroup.getName(), groupName) && setGroup.getType() == groupType)
                .findAny();
        ModelSetsGroupEntity setsGroup = getSetsGroupFromOptional("[" + groupName + "," + groupType.name() + "]", modelSetsGroupOpt);

        // do not throw exception even the given set name is not found
        Optional<ModelParameterSetEntity> modelSetOpt = setsGroup.getSets().stream()
                .filter(set -> StringUtils.equals(set.getName(), setName))
                .findAny();
        modelSetOpt.ifPresent(set -> {
            setsGroup.removeParameterSet(set);
            modelRepository.save(modelToUpdate);
        });

        return new ParametersSetsGroup(setsGroup);
    }

    // --- BEGIN parameter definition-related service methods --- //

    @Override
    @Transactional(readOnly = true)
    public List<String> getParameterDefinitionNames() {
        return modelParameterDefinitionRepository.findAll().stream().map(ModelParameterDefinitionEntity::getName).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelParameterDefinition> getParameterDefinitions(List<String> parameterDefinitionNames) {
        return modelParameterDefinitionRepository.findAllByNameIn(parameterDefinitionNames).stream()
                .map(parameterDefinitionEntity -> new ModelParameterDefinition(parameterDefinitionEntity, null, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelParameterDefinition> getParameterDefinitionsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getParameterDefinitions();
    }

    @Override
    @Transactional
    public Model addNewParameterDefinitionsToModel(String modelName, List<ModelParameterDefinition> parameterDefinitions) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of parameter definitions
        if (!CollectionUtils.isEmpty(parameterDefinitions)) {
            // do merge with existing list
            parameterDefinitions.forEach(parameterDefinition ->
                modelToUpdate.addParameterDefinition(new ModelParameterDefinitionEntity(parameterDefinition), parameterDefinition.getOrigin(), parameterDefinition.getOriginName()));
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model addExistingParameterDefinitionsToModel(String modelName, List<String> parameterDefinitionNames, ParameterOrigin origin, String originName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of parameter definitions
        if (!CollectionUtils.isEmpty(parameterDefinitionNames)) {
            // find existing parameter definitions
            List<ModelParameterDefinitionEntity> foundParameterDefinitionEntities = modelParameterDefinitionRepository.findAllByNameIn(parameterDefinitionNames);

            // check whether found all, fail fast
            if (foundParameterDefinitionEntities.size() != parameterDefinitionNames.size()) {
                List<String> foundNames = foundParameterDefinitionEntities.stream()
                        .map(ModelParameterDefinitionEntity::getName).toList();
                List<String> notFoundNames = parameterDefinitionNames.stream().filter(name -> !foundNames.contains(name)).toList();
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some parameter definition not found: " + notFoundNames);
            }

            // do merge with existing list
            modelToUpdate.addAllParameterDefinition(foundParameterDefinitionEntities, origin, originName);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    @Transactional
    public Model removeExistingParameterDefinitionsFromModel(String modelName, List<String> parameterDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do remove in the list of parameter definitions
        if (!CollectionUtils.isEmpty(parameterDefinitionNames)) {
            // find existing variable definitions
            List<ModelParameterDefinitionEntity> foundParameterDefinitionEntities = modelParameterDefinitionRepository.findAllByNameIn(parameterDefinitionNames);

            // remove in existing list
            modelToUpdate.removeAllParameterDefinition(foundParameterDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    @Transactional
    public Model removeAllParameterDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // clear the existing list
        modelToUpdate.removeAllParameterDefinition(modelToUpdate.getParameterDefinitions().stream()
                .map(ModelModelParameterDefinitionEntity::getParameterDefinition).toList());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public List<ModelParameterDefinition> saveNewParameterDefinitions(List<ModelParameterDefinition> parameterDefinitions) {
        if (!CollectionUtils.isEmpty(parameterDefinitions)) {
            Set<ModelParameterDefinitionEntity> parameterDefinitionEntities = parameterDefinitions.stream()
                    .map(ModelParameterDefinitionEntity::new)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<ModelParameterDefinitionEntity> savedParameterDefinitionEntities = modelParameterDefinitionRepository.saveAll(parameterDefinitionEntities);
            return savedParameterDefinitionEntities.stream().map(entity -> new ModelParameterDefinition(entity, null, null)).toList();
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public List<String> deleteParameterDefinitions(List<String> parameterDefinitionNames) {
        if (!CollectionUtils.isEmpty(parameterDefinitionNames)) {
            modelParameterDefinitionRepository.deleteAllByNameIn(parameterDefinitionNames);
        }
        return parameterDefinitionNames;
    }

    // --- END parameter definition-related service methods --- //

    // --- BEGIN variable-related service methods --- //
    @Override
    @Transactional(readOnly = true)
    public List<ModelVariableDefinition> getVariableDefinitionsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getVariableDefinitions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVariableDefinitionNames() {
        return modelVariableRepository.findAll().stream().map(ModelVariableDefinitionEntity::getName).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ModelVariableDefinition> getVariableDefinitions(List<String> variableNames) {
        return modelVariableRepository.findAllByNameIn(variableNames).stream().map(ModelVariableDefinition::new).toList();
    }

    @Override
    @Transactional
    public Model addNewVariableDefinitionsToModel(String modelName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variable definitions
        if (!CollectionUtils.isEmpty(variableDefinitions)) {
            // do merge with existing list
            List<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream()
                    .map(variableDefinition -> new ModelVariableDefinitionEntity(modelToUpdate, null, variableDefinition))
                    .toList();
            modelToUpdate.addAllVariableDefinition(variableDefinitionEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    @Transactional
    public Model addExistingVariableDefinitionsToModel(String modelName, List<String> variableDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variable definitions
        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllByNameIn(variableDefinitionNames);

            // check whether found all, fail fast
            if (foundVariableDefinitionEntities.size() != variableDefinitionNames.size()) {
                List<String> foundNames = foundVariableDefinitionEntities.stream()
                        .map(ModelVariableDefinitionEntity::getName).toList();
                List<String> notFoundNames = variableDefinitionNames.stream().filter(name -> !foundNames.contains(name)).toList();
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some variable definition not found: " + notFoundNames);
            }

            // do merge with existing list
            modelToUpdate.addAllVariableDefinition(foundVariableDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model removeExistingVariableDefinitionsFromModel(String modelName, List<String> variableDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do remove in the list of variable definitions
        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllByNameIn(variableDefinitionNames);

            // remove in existing list
            modelToUpdate.removeAllVariableDefinition(foundVariableDefinitionEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public List<ModelVariableDefinition> saveNewVariableDefinitions(List<ModelVariableDefinition> variableDefinitions) {
        if (!CollectionUtils.isEmpty(variableDefinitions)) {
            Set<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream()
                    .map(variableDefinition -> new ModelVariableDefinitionEntity(null, null, variableDefinition))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<ModelVariableDefinitionEntity> savedVariableDefinitionEntities = modelVariableRepository.saveAll(variableDefinitionEntities);
            return savedVariableDefinitionEntities.stream().map(ModelVariableDefinition::new).toList();
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Model removeAllVariableDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

         // clear the existing list
        modelToUpdate.removeAllVariableDefinition(modelToUpdate.getVariableDefinitions());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getVariablesSetNames() {
        return modelVariablesSetRepository.findAll().stream().map(ModelVariableSetEntity::getName).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariablesSet> getVariablesSets(List<String> variablesSetNames) {
        return modelVariablesSetRepository.findAllByNameIn(variablesSetNames).stream().map(VariablesSet::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariablesSet> getVariablesSetsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getVariablesSets();
    }

    @Override
    @Transactional
    public VariablesSet saveNewVariablesSet(VariablesSet variableSet) {
        ModelVariableSetEntity variableSetEntity = new ModelVariableSetEntity(null, variableSet);

        // process reusable entities
        Set<ModelVariableDefinitionEntity> variableDefinitions = variableSetEntity.getVariableDefinitions();
        if (!CollectionUtils.isEmpty(variableDefinitions)) {
            List<String> variableNamesInSet = variableDefinitions.stream().map(ModelVariableDefinitionEntity::getName).toList();
            List<ModelVariableDefinitionEntity> existingVariables = modelVariableRepository.findAllByNameIn(variableNamesInSet);

            // Indexing by variable's name
            Map<String, ModelVariableDefinitionEntity> existingVariablesMap = existingVariables.stream()
                    .collect(Collectors.toMap(ModelVariableDefinitionEntity::getName, Function.identity()));
            Set<ModelVariableDefinitionEntity> mergedVariables = new LinkedHashSet<>();
            // perform merge with existing entities
            for (ModelVariableDefinitionEntity variable : variableDefinitions) {
                ModelVariableDefinitionEntity mergedVariable = variable;
                if (existingVariablesMap.get(variable.getName()) != null) {
                    mergedVariable = existingVariablesMap.get(variable.getName());
                    if (mergedVariable.getVariablesSets() != null) {
                        mergedVariable.setVariablesSets(new LinkedHashSet<>());
                    }
                    mergedVariable.getVariablesSets().add(variableSetEntity);
                }
                mergedVariables.add(mergedVariable);
            }
            // use merged result
            variableSetEntity.setVariableDefinitions(mergedVariables);
        }

        // rely on Hibernate to save
        ModelVariableSetEntity savedVariableSetEntity = modelVariablesSetRepository.save(variableSetEntity);
        return new VariablesSet(savedVariableSetEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModelVariableDefinition> getVariableDefinitionsFromVariablesSet(String variableSetName) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findByName(variableSetName);

        ModelVariableSetEntity variableSetEntity = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        VariablesSet variablesSet = new VariablesSet(variableSetEntity);
        return variablesSet.getVariableDefinitions();
    }

    @Override
    @Transactional
    public VariablesSet addNewVariableDefinitionToVariablesSet(String variableSetName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findByName(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        if (!CollectionUtils.isEmpty(variableDefinitions)) {
            // do merge with existing list
            List<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream()
                    .map(variableDefinition -> new ModelVariableDefinitionEntity(null, variableSetToUpdate, variableDefinition))
                    .toList();
            variableSetToUpdate.addAllVariableDefinition(variableDefinitionEntities);
            // save modified existing variables set entity
            modelVariablesSetRepository.save(variableSetToUpdate);
        }

        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet removeExistingVariableDefinitionFromVariablesSet(String variableSetName, List<String> variableDefinitionNames) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findByName(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllByNameIn(variableDefinitionNames);

            // remove in existing list
            variableSetToUpdate.removeAllVariableDefinition(foundVariableDefinitionEntities);

            // save modified existing variables set entity
            modelVariablesSetRepository.save(variableSetToUpdate);
        }

        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet removeAllVariableDefinitionOnVariablesSet(String variableSetName) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findByName(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        // clear the existing list
        variableSetToUpdate.removeAllVariableDefinition(variableSetToUpdate.getVariableDefinitions());

        // save modified existing variables set entity
        modelVariablesSetRepository.save(variableSetToUpdate);
        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public Model addNewVariablesSetsToModel(String modelName, List<VariablesSet> variableSets) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (!CollectionUtils.isEmpty(variableSets)) {
            // do merge with existing list
            List<ModelVariableSetEntity> variablesSetEntities = variableSets.stream()
                    .map(variablesSet -> new ModelVariableSetEntity(modelToUpdate, variablesSet))
                    .toList();
            modelToUpdate.addAllVariablesSet(variablesSetEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model addExistingVariablesSetsToModel(String modelName, List<String> variablesSetNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (!CollectionUtils.isEmpty(variablesSetNames)) {
            // do merge with existing list
            List<ModelVariableSetEntity> foundVariablesSetEntities = modelVariablesSetRepository.findAllByNameIn(variablesSetNames);

            // check whether found all
            if (foundVariablesSetEntities.size() != variablesSetNames.size()) {
                List<String> foundNames = foundVariablesSetEntities.stream().map(ModelVariableSetEntity::getName).toList();
                List<String> notFoundNames = variablesSetNames.stream().filter(name -> !foundNames.contains(name)).toList();
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some variables set not found: " + notFoundNames);
            }

            modelToUpdate.addAllVariablesSet(foundVariablesSetEntities);
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model removeExistingVariablesSetsFromModel(String modelName, List<String> variablesSetNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (!CollectionUtils.isEmpty(variablesSetNames)) {
            // remove from existing list
            List<ModelVariableSetEntity> foundVariablesSetEntities = modelVariablesSetRepository.findAllByNameIn(variablesSetNames);
            modelToUpdate.removeAllVariablesSet(foundVariablesSetEntities);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model removeAllExistingVariablesSetsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findByModelName(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        modelToUpdate.removeAllVariablesSet(modelToUpdate.getVariableSets());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public List<String> deleteVariableDefinitions(List<String> variableDefinitionNames) {
        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            modelVariableRepository.deleteAllByNameIn(variableDefinitionNames);
        }

        return variableDefinitionNames;
    }

    @Override
    @Transactional
    public List<String> deleteVariablesSets(List<String> variablesSetNames) {
        if (!CollectionUtils.isEmpty(variablesSetNames)) {
            modelVariablesSetRepository.deleteAllByNameIn(variablesSetNames);
        }

        return variablesSetNames;
    }
    // --- END variable-related service methods --- //

}

