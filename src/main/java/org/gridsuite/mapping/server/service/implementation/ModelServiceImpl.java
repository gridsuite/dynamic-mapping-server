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
import org.gridsuite.mapping.server.repository.ModelParameterDefinitionRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ModelVariableRepository;
import org.gridsuite.mapping.server.repository.ModelVariablesSetRepository;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private final ModelParameterDefinitionRepository modelParameterDefinitionRepository;
    private final ModelVariableRepository modelVariableRepository;
    private final ModelVariablesSetRepository modelVariablesSetRepository;

    @Autowired
    public ModelServiceImpl(
            ResourcePatternResolver resourcePatternResolver,
            ObjectMapper objectMapper,
            ModelRepository modelRepository,
            ModelParameterDefinitionRepository modelParameterDefinitionRepository,
            ModelVariableRepository modelVariableRepository,
            ModelVariablesSetRepository modelVariablesSetRepository
    ) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.objectMapper = objectMapper;
        this.modelRepository = modelRepository;
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
    public List<SimpleModel> getModels() {
        return modelRepository.findAll().stream().map(modelEntity -> new SimpleModel(new Model(modelEntity))).collect(Collectors.toList());
    }

    @Override
    public List<ParametersSet> getSetsFromGroup(String modelName, String groupName, SetGroupType groupType) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Optional<ModelSetsGroupEntity> modelSetsGroupOpt = modelEntity.getSetsGroups().stream()
                .filter(setGroup -> StringUtils.equals(setGroup.getName(), groupName) && setGroup.getType() == groupType)
                .findAny();
        ModelSetsGroupEntity setsGroup = getSetsGroupFromOptional("[" + groupName + "," + groupType.name() + "]", modelSetsGroupOpt);

        return setsGroup.getSets().stream().map(ParametersSet::new).collect(Collectors.toList());
    }

    @Override
    public ParametersSetsGroup saveParametersSetsGroup(String modelName, ParametersSetsGroup setsGroup, Boolean strict) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

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
    }

    @Override
    @Transactional
    public Model saveModel(Model model) {
        modelRepository.save(new ModelEntity(model));
        return model;
    }

    @Override
    public ParametersSetsGroup deleteSet(String modelName, String groupName, SetGroupType groupType, String setName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        Optional<ModelSetsGroupEntity> modelSetsGroupOpt = modelToUpdate.getSetsGroups().stream()
                .filter(setGroup -> StringUtils.equals(setGroup.getName(), groupName) && setGroup.getType() == groupType)
                .findAny();
        ModelSetsGroupEntity setsGroup = getSetsGroupFromOptional("[" + groupName + "," + groupType.name() + "]", modelSetsGroupOpt);
        List<ModelParameterSetEntity> sets = setsGroup.getSets();
        setsGroup.setSets(sets.stream().filter(set -> !set.getName().equals(setName)).collect(Collectors.toList()));

        modelRepository.save(modelToUpdate);
        return new ParametersSetsGroup(setsGroup);
    }

    // --- BEGIN parameter definition-related service methods --- //
    @Override
    public List<ModelParameterDefinition> getParameterDefinitionsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getParameterDefinitions();
    }

    @Override
    @Transactional
    public Model addNewParameterDefinitionsToModel(String modelName, List<ModelParameterDefinition> parameterDefinitions) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of parameter definitions
        if (!CollectionUtils.isEmpty(parameterDefinitions)) {
            // do merge with existing list
            parameterDefinitions.forEach(parameterDefinition ->
                modelToUpdate.addParameterDefinition(new ModelParameterDefinitionEntity(parameterDefinition), parameterDefinition.getOrigin()));
            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public Model addExistingParameterDefinitionsToModel(String modelName, List<String> parameterDefinitionNames, ParameterOrigin origin) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of parameter definitions
        if (!CollectionUtils.isEmpty(parameterDefinitionNames)) {
            // find existing parameter definitions
            List<ModelParameterDefinitionEntity> foundParameterDefinitionEntities = modelParameterDefinitionRepository.findAllById(parameterDefinitionNames);

            // check whether found all, fail fast
            if (foundParameterDefinitionEntities.size() != parameterDefinitionNames.size()) {
                List<String> foundNames = foundParameterDefinitionEntities.stream()
                        .map(ModelParameterDefinitionEntity::getName).collect(Collectors.toList());
                List<String> notFoundNames = parameterDefinitionNames.stream().filter(name -> !foundNames.contains(name)).collect(Collectors.toList());
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Some parameter definition not found: " + notFoundNames);
            }

            // do merge with existing list
            modelToUpdate.addParameterDefinitions(foundParameterDefinitionEntities, origin);

            // save modified existing model entity
            modelRepository.save(modelToUpdate);
        }

        return new Model(modelToUpdate);

    }

    @Override
    @Transactional
    public Model removeExistingParameterDefinitionsFromModel(String modelName, List<String> parameterDefinitionNames) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do remove in the list of parameter definitions
        if (!CollectionUtils.isEmpty(parameterDefinitionNames)) {
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
    @Transactional
    public Model removeAllParameterDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // clear the existing list
        modelToUpdate.removeParameterDefinitions(modelToUpdate.getParameterDefinitions().stream()
                .map(ModelModelParameterDefinitionEntity::getParameterDefinition).collect(Collectors.toList()));

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
            return savedParameterDefinitionEntities.stream().map(entity -> new ModelParameterDefinition(entity, null)).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public List<String> deleteParameterDefinitions(List<String> parameterDefinitionNames) {
        if (!CollectionUtils.isEmpty(parameterDefinitionNames)) {
            modelParameterDefinitionRepository.deleteAllById(parameterDefinitionNames);
        }
        return parameterDefinitionNames;
    }

    // --- END parameter definition-related service methods --- //

    // --- BEGIN variable-related service methods --- //
    @Override
    public List<ModelVariableDefinition> getVariableDefinitionsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getVariableDefinitions();
    }

    @Override
    @Transactional
    public Model addNewVariableDefinitionsToModel(String modelName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variable definitions
        if (!CollectionUtils.isEmpty(variableDefinitions)) {
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

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variable definitions
        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // check whether found all, fail fast
            if (foundVariableDefinitionEntities.size() != variableDefinitionNames.size()) {
                List<String> foundNames = foundVariableDefinitionEntities.stream()
                        .map(ModelVariableDefinitionEntity::getName).collect(Collectors.toList());
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

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do remove in the list of variable definitions
        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
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
        if (!CollectionUtils.isEmpty(variableDefinitions)) {
            Set<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream()
                    .map(variableDefinition -> new ModelVariableDefinitionEntity(null, null, variableDefinition))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<ModelVariableDefinitionEntity> savedVariableDefinitionEntities = modelVariableRepository.saveAll(variableDefinitionEntities);
            return savedVariableDefinitionEntities.stream().map(ModelVariableDefinition::new).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Model removeAllVariableDefinitionsOnModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

         // clear the existing list
        modelToUpdate.removeVariableDefinitions(modelToUpdate.getVariableDefinitions());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    public List<VariablesSet> getVariablesSetsFromModel(String modelName) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelEntity = getModelFromOptional(modelName, foundModelOpt);

        Model modelToSend = new Model(modelEntity);
        return modelToSend.getVariablesSets();
    }

    @Override
    @Transactional
    public VariablesSet saveNewVariablesSet(VariablesSet variableSet) {
        ModelVariableSetEntity variableSetEntity = new ModelVariableSetEntity(null, variableSet);
        ModelVariableSetEntity savedVariableSetEntity = modelVariablesSetRepository.save(variableSetEntity);
        return new VariablesSet(savedVariableSetEntity);
    }

    @Override
    public List<ModelVariableDefinition> getVariableDefinitionsFromVariablesSet(String variableSetName) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetEntity = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        VariablesSet variablesSet = new VariablesSet(variableSetEntity);
        return variablesSet.getVariableDefinitions();
    }

    @Override
    @Transactional
    public VariablesSet addNewVariableDefinitionToVariablesSet(String variableSetName, List<ModelVariableDefinition> variableDefinitions) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        if (!CollectionUtils.isEmpty(variableDefinitions)) {
            // do merge with existing list
            List<ModelVariableDefinitionEntity> variableDefinitionEntities = variableDefinitions.stream()
                    .map(variableDefinition -> new ModelVariableDefinitionEntity(null, variableSetToUpdate, variableDefinition))
                    .collect(Collectors.toList());
            variableSetToUpdate.addVariableDefinitions(variableDefinitionEntities);
            // save modified existing variables set entity
            modelVariablesSetRepository.save(variableSetToUpdate);
        }

        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet removeExistingVariableDefinitionFromVariablesSet(String variableSetName, List<String> variableDefinitionNames) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            // find existing variable definitions
            List<ModelVariableDefinitionEntity> foundVariableDefinitionEntities = modelVariableRepository.findAllById(variableDefinitionNames);

            // remove in existing list
            variableSetToUpdate.removeVariableDefinitions(foundVariableDefinitionEntities);

            // save modified existing variables set entity
            modelVariablesSetRepository.save(variableSetToUpdate);
        }

        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public VariablesSet removeAllVariableDefinitionOnVariablesSet(String variableSetName) {
        Optional<ModelVariableSetEntity> foundVariableSetOpt = modelVariablesSetRepository.findById(variableSetName);

        ModelVariableSetEntity variableSetToUpdate = getVariableSetFromOptional(variableSetName, foundVariableSetOpt);

        // clear the existing list
        variableSetToUpdate.removeVariableDefinitions(variableSetToUpdate.getVariableDefinitions());

        // save modified existing variables set entity
        modelVariablesSetRepository.save(variableSetToUpdate);
        return new VariablesSet(variableSetToUpdate);
    }

    @Override
    @Transactional
    public Model addNewVariablesSetsToModel(String modelName, List<VariablesSet> variableSets) {
        Optional<ModelEntity> foundModelOpt = modelRepository.findById(modelName);

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (!CollectionUtils.isEmpty(variableSets)) {
            // do merge with existing list
            List<ModelVariableSetEntity> variablesSetEntities = variableSets.stream()
                    .map(variablesSet -> new ModelVariableSetEntity(modelToUpdate, variablesSet))
                    .collect(Collectors.toList());
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

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (!CollectionUtils.isEmpty(variablesSetNames)) {
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

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        // do merge with the list of variables set
        if (!CollectionUtils.isEmpty(variablesSetNames)) {
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

        ModelEntity modelToUpdate = getModelFromOptional(modelName, foundModelOpt);

        modelToUpdate.removeVariablesSets(modelToUpdate.getVariableSets());

        // save modified existing model entity
        modelRepository.save(modelToUpdate);

        return new Model(modelToUpdate);
    }

    @Override
    @Transactional
    public List<String> deleteVariableDefinitions(List<String> variableDefinitionNames) {
        if (!CollectionUtils.isEmpty(variableDefinitionNames)) {
            modelVariableRepository.deleteAllById(variableDefinitionNames);
        }

        return variableDefinitionNames;
    }

    @Override
    @Transactional
    public List<String> deleteVariablesSets(List<String> variablesSetNames) {
        if (!CollectionUtils.isEmpty(variablesSetNames)) {
            modelVariablesSetRepository.deleteAllById(variablesSetNames);
        }

        return variablesSetNames;
    }
    // --- END variable-related service methods --- //

}

