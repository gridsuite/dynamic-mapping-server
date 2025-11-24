/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.gridsuite.mapping.server.dto.models.*;
import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.ModelParameterDefinitionRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ModelVariableRepository;
import org.gridsuite.mapping.server.repository.ModelVariablesSetRepository;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.gridsuite.mapping.server.utils.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
@TestPropertySource(properties = {"ex-resources.automaton = src/test/resources/data/ex-automaton"})
public class ModelControllerTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(ModelControllerTest.class);

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelParameterDefinitionRepository modelParameterDefinitionRepository;

    @Autowired
    private ModelVariableRepository modelVariableRepository;
    @Autowired
    private ModelVariablesSetRepository modelVariablesSetRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    public void cleanDB() {
        // delete from parent to child
        modelRepository.deleteAll();
        modelVariablesSetRepository.deleteAll();
        modelVariableRepository.deleteAll();
        modelParameterDefinitionRepository.deleteAll();
    }

    private ModelParameterDefinitionEntity createDefinitionEntity(String name, ParameterType type) {
        return new ModelParameterDefinitionEntity(new ModelParameterDefinition(UUID.randomUUID(), name, type, null, null, null));
    }

    @Before
    public void setUp() {
        cleanDB();

        // prepare token model
        ModelEntity modelToSave = new ModelEntity(UUID.randomUUID(), "LoadAlphaBeta", EquipmentType.LOAD, false,
                new ArrayList<>(), new ArrayList<>(), Set.of(), Set.of(), null, null);
        List<ModelParameterDefinitionEntity> definitions = new ArrayList<>();

        // add "USER" parameter definitions
        definitions.add(createDefinitionEntity("load_alpha", ParameterType.DOUBLE));
        definitions.add(createDefinitionEntity("load_beta", ParameterType.DOUBLE));
        modelToSave.addAllParameterDefinition(definitions, ParameterOrigin.USER, null);

        definitions.clear();

        // add "NETWORK" parameter definitions
        modelToSave.addParameterDefinition(createDefinitionEntity("load_P0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "p_pu");
        modelToSave.addParameterDefinition(createDefinitionEntity("load_Q0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "q_pu");
        modelToSave.addParameterDefinition(createDefinitionEntity("load_U0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "v_pu");
        modelToSave.addParameterDefinition(createDefinitionEntity("load_UPhase0", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "angle_pu");

        modelRepository.save(modelToSave);
    }

    @Test
    public void testGetAutomatonDefinitions() throws Exception {
        MvcResult mvcResult = mvc.perform(get("/models/automaton-definitions")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        String automatonsJsonResult = mvcResult.getResponse().getContentAsString();

        LOGGER.info("Automatons result in Json array = \n {}", automatonsJsonResult);

        // Check result
        JsonNode jsonNode = objectMapper.readTree(automatonsJsonResult);
        Assertions.assertThat(jsonNode.isArray()).isTrue();
        Assertions.assertThat(jsonNode.size()).isEqualTo(3);
    }

    @Test
    @Transactional
    public void test() throws Exception {

        String name = "setName";
        String modelName = "LoadAlphaBeta";
        String set = """
                {
                    "name": "%s",
                    "parameters": [
                        {
                            "name": "load_alpha",
                            "value": "1.5"
                        },
                        {
                            "name": "load_beta",
                            "value": "2.5"
                        }
                    ]
                }
            """
            .formatted(name);
        String setGroup = """
                {
                    "name": "%s",
                    "modelName": "%s",
                    "type": "FIXED",
                    "sets": [
                        %s
                    ]
                }
            """
            .formatted(name, modelName, set);

        // Put data
        mvc.perform(post("/models/" + modelName + "/parameters/sets/strict")
                        .content(setGroup)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        String setJson = mvcResult.getResponse().getContentAsString();
        List<ParametersSet> resultSetList = objectMapper.readValue(setJson, new TypeReference<>() { });
        ParametersSet expectedSet = objectMapper.readValue(set, ParametersSet.class);

        Assertions.assertThat(resultSetList.getFirst()).recursivelyEquals(expectedSet);

        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "PREFIX")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Date setCreationDate = modelRepository.findByModelName(modelName).get().getSetsGroups().getFirst().getSets().getFirst().getLastModifiedDate();

        // Update data
        mvc.perform(post("/models/" + modelName + "/parameters/sets/strict")
                        .content(setGroup)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        Date setUpdateDate = modelRepository.findByModelName(modelName).get().getSetsGroups().getFirst().getSets().getFirst().getLastModifiedDate();

        Assertions.assertThat(setCreationDate.compareTo(setUpdateDate) < 0).isTrue();
    }

    @Test
    public void definitionTest() throws Exception {
        String modelName = "LoadAlphaBeta";

        String expectedParamaterDefinitionsJson = """
                    [
                        {"name": "load_alpha", "type": "DOUBLE", "origin": "USER", "originName": null, "fixedValue": null},
                        {"name": "load_beta", "type": "DOUBLE", "origin": "USER", "originName": null, "fixedValue": null},
                        {"name": "load_P0Pu", "type": "DOUBLE", "origin": "NETWORK", "originName": "p_pu", "fixedValue": null},
                        {"name": "load_Q0Pu", "type": "DOUBLE", "origin": "NETWORK", "originName": "q_pu", "fixedValue": null},
                        {"name": "load_U0Pu", "type": "DOUBLE", "origin": "NETWORK", "originName": "v_pu", "fixedValue": null},
                        {"name": "load_UPhase0", "type": "DOUBLE", "origin": "NETWORK", "originName": "angle_pu", "fixedValue": null}
                    ]
                """;
        MvcResult mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/definitions")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        String resultParameterDefinitionsJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(mvcResult.getResponse().getContentAsString()));
        LOGGER.info("Result parameter definitions in json array \n {}", resultParameterDefinitionsJson);
        LOGGER.info("Expected parameter definitions in json array \n {}", expectedParamaterDefinitionsJson);

        List<ModelParameterDefinition> actual = objectMapper.readValue(resultParameterDefinitionsJson, new TypeReference<>() { });
        List<ModelParameterDefinition> expected = objectMapper.readValue(expectedParamaterDefinitionsJson, new TypeReference<>() { });
        Assertions.assertThatDtoList(actual).recursivelyContainAll(expected);

        // --- GET all names of parameter definitions in the database --- //
        mvcResult = mvc.perform(get("/models/parameter-definitions/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        // must contain 6 names of parameter definitions in the database
        List<String> foundParameterDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() { });
        assertThat(foundParameterDefinitionNames).hasSize(6);

        // --- GET all parameter definitions in the database --- //
        mvcResult = mvc.perform(get("/models/parameter-definitions")
                        .contentType(APPLICATION_JSON)
                        .param("parameterDefinitionNames", String.join(",", foundParameterDefinitionNames)))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        // must contain 6 parameter definitions in the database
        List<ModelParameterDefinition> foundParameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() { });
        assertThat(foundParameterDefinitions).hasSize(6);
    }

    @Test
    public void invalidTest() throws Exception {

        String name = "errorSet";
        String modelName = "LoadAlphaBeta";
        String set = """
                {
                    "name": "%s",
                    "modelName": "%s",
                    "parameters": [
                        {
                            "name": "load_alpha",
                            "value": "1.5"
                        }
                    ]
                }
            """
            .formatted(name, modelName);
        // Put data
        String setGroup = """
                {
                    "name": "%s",
                    "modelName": "%s",
                    "type": "FIXED",
                    "sets": [
                        %s
                    ]
                }
            """
            .formatted(name, modelName, set);

        mvc.perform(post("/models/" + modelName + "/parameters/sets/strict")
                        .content(setGroup)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void getTest() throws Exception {

        // Prepare models
        ModelEntity loadModel = modelRepository.findByModelName("LoadAlphaBeta").get();
        List<ModelSetsGroupEntity> loadGroups = loadModel.getSetsGroups();
        ModelSetsGroupEntity loadGroup = new ModelSetsGroupEntity(UUID.randomUUID(), "LAB", null, SetGroupType.FIXED, loadModel);
        ArrayList<ModelParameterSetEntity> groupSets = new ArrayList<>();
        ModelParameterSetEntity setToSave = new ModelParameterSetEntity(UUID.randomUUID(), "LAB", null, new Date(), loadGroup);
        ArrayList<ModelParameterEntity> setParameters = new ArrayList<>();
        setParameters.add(new ModelParameterEntity(UUID.randomUUID(), "load_alpha", "1.5", setToSave));
        setParameters.add(new ModelParameterEntity(UUID.randomUUID(), "load_beta", "2.5", setToSave));
        setToSave.setParameters(setParameters);
        groupSets.add(setToSave);
        loadGroup.setSets(groupSets);
        loadGroups.add(loadGroup);
        loadModel.setSetsGroups(loadGroups);
        modelRepository.save(loadModel);

        ModelEntity generatorThreeModel = new ModelEntity(UUID.randomUUID(), "GeneratorThreeWindings", EquipmentType.GENERATOR, false, List.of(), null, Set.of(), Set.of(), null, null);
        ArrayList<ModelSetsGroupEntity> generatorThreeGroups = new ArrayList<>();
        generatorThreeGroups.add(new ModelSetsGroupEntity(UUID.randomUUID(), "GSTWPR", new ArrayList<>(), SetGroupType.PREFIX, generatorThreeModel));
        generatorThreeModel.setSetsGroups(generatorThreeGroups);
        modelRepository.save(generatorThreeModel);

        String expectedModelsJson = """
                    [
                        {
                            "name": "LoadAlphaBeta",
                            "type": "LOAD",
                            "groups": [
                                {"name": "LAB", "type": "FIXED", "setsNumber": 1}
                            ]
                        },
                        {
                            "name": "GeneratorThreeWindings",
                            "type": "GENERATOR",
                            "groups": [
                                {"name": "GSTWPR", "type": "PREFIX", "setsNumber": 0}
                            ]
                        }
                    ]
                """;
        MvcResult mvcResult = mvc.perform(get("/models/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        String resultModelsJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(mvcResult.getResponse().getContentAsString()));
        LOGGER.info("Result models in json array = \n {}", resultModelsJson);
        LOGGER.info("Expected models in json array = \n {}", expectedModelsJson);
        List<SimpleModel> resultModels = objectMapper.readValue(resultModelsJson, new TypeReference<>() { });
        List<SimpleModel> expectedModels = objectMapper.readValue(expectedModelsJson, new TypeReference<>() { });
        Assertions.assertThatDtoList(resultModels).recursivelyContainAll(expectedModels);

        // --- GET all names of models in the database --- //
        mvcResult = mvc.perform(get("/models/names")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        List<String> resultModelNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        Assertions.assertThat(resultModelNames).containsExactlyInAnyOrder("LoadAlphaBeta", "GeneratorThreeWindings");

        // --- GET parameter set groups in the database --- //
        mvcResult = mvc.perform(get("/models/parameters-sets-groups")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        // check result
        // must contain 2 parameter set groups, i.e. "LAB" and "GSTWPR"
        List<ParametersSetsGroup> foundParametersSetsGroups = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundParametersSetsGroups).hasSize(2);

        // --- GET parameter sets in the database --- //
        mvcResult = mvc.perform(get("/models/parameters-sets")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        // check result
        // must contain only 1 parameter set, i.e. "LAB"
        List<ParametersSet> foundParametersSets = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundParametersSets).hasSize(1);

        // --- GET parameters in the database --- //
        mvcResult = mvc.perform(get("/models/parameters")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        // check result
        // must contain 2 parameter, i.e. "load_alpha", "load_beta"
        List<ModelParameter> foundParameters = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundParameters).hasSize(2);
    }

    public static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    @Test
    public void testSaveLoadModelThenModifyParameterDefinitions() throws Exception {
        String modelName = "LoadAlphaBeta";
        String newModelJson = readFileAsString("src/test/resources/data/model/load/loadAlphaBeta.json");
        String newParameterDefinitionsJson = readFileAsString("src/test/resources/data/model/load/loadAlphaBeta_parameter_definitions.json");

        cleanDB();
        // --- Put data first time with initial parameter definitions --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get initial parameter definitions
        List<ModelParameterDefinition> parameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        assertThat(parameterDefinitions).hasSize(6);

        // --- Get initial parameter definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/definitions"))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> parameterDefinitions1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(parameterDefinitions1).hasSize(6);

        // --- Try to get parameter definitions from unknown model --- //
        mvc.perform(get("/models/" + modelName + "_unknown" + "/parameters/definitions"))
                .andExpect(status().isNotFound());

        // --- Put data second time which add only a parameter definition --- //
        mvcResult = mvc.perform(post("/models/" + modelName + "/parameters/definitions")
                        .content(newParameterDefinitionsJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current parameter definitions
        List<ModelParameterDefinition> parameterDefinitions2 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Initial parameter definitions = {}", parameterDefinitions);
        LOGGER.info("Updated parameter definitions = {}", parameterDefinitions2);

        // check result
        // final model's parameter definitions must contain all ones of initial model
        assertThat(parameterDefinitions2.size() - parameterDefinitions.size()).isOne();
        Assertions.assertThatDtoList(parameterDefinitions2).recursivelyContainAll(parameterDefinitions);

        // --- Remove an existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/parameters/definitions/remove")
                        .content(objectMapper.writeValueAsString(List.of(parameterDefinitions2.get(5).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current parameter definitions
        List<ModelParameterDefinition> parameterDefinitions3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Updated parameter definitions = {}", parameterDefinitions2);
        LOGGER.info("Removed parameter definitions = {}", parameterDefinitions3);

        // check result
        // final model's parameter definitions must contain all ones of model
        assertThat(parameterDefinitions2.size() - parameterDefinitions3.size()).isOne();
        Assertions.assertThatDtoList(parameterDefinitions2).recursivelyContainAll(parameterDefinitions3);

        // --- Remove all parameter definitions --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/parameters/definitions/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelParameterDefinition> parameterDefinitions4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Unset parameter definitions = {}", parameterDefinitions4);

        // check result
        // must have no parameter definition
        assertThat(parameterDefinitions4).isEmpty();

        // --- Save new parameter definition --- //
        List<ModelParameterDefinition> parameterDefinitionList = objectMapper.readValue(newParameterDefinitionsJson, new TypeReference<>() { });
        parameterDefinitionList.getFirst().setName("load_UPhase0_3");
        mvcResult = mvc.perform(post("/models/parameters/definitions")
                        .content(objectMapper.writeValueAsString(parameterDefinitionList))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> savedParameterDefinitionList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });

        // check result
        // must have the same number of input variable definitions
        assertThat(savedParameterDefinitionList).hasSameSizeAs(parameterDefinitionList);

        // --- Add existing parameter definition to model --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/parameters/definitions/add?origin=USER")
                        .content(objectMapper.writeValueAsString(savedParameterDefinitionList.stream().map(ModelParameterDefinition::getName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current parameter definitions in the model
        List<ModelParameterDefinition> parameterDefinitions6 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Updated parameter definitions = {}", parameterDefinitions6);
        // must have the same number of above input parameter definitions
        assertThat(parameterDefinitions6).hasSameSizeAs(savedParameterDefinitionList);

        // --- Add unknown existing parameter definition to model => must fail fast --- //
        mvc.perform(patch("/models/" + modelName + "/parameters/definitions/add")
                        .content(objectMapper.writeValueAsString(List.of("parameter_definition_unknown")))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // --- Delete definitively a parameter definition --- //
        mvcResult = mvc.perform(delete("/models/parameters/definitions")
                        .content(objectMapper.writeValueAsString(List.of(parameterDefinitions.get(4).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> deletedParameterDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        LOGGER.info("Deleted parameter definitions = {}", deletedParameterDefinitionNames);

        // Check result
        assertThat(deletedParameterDefinitionNames).hasSize(1);
        assertThat(deletedParameterDefinitionNames.getFirst()).isEqualTo(parameterDefinitions.get(4).getName());
    }

    @Test
    public void testSaveLoadModelThenModifyVariableDefinitions() throws Exception {

        String modelName = "LoadAlphaBeta";
        String newModelJson = readFileAsString("src/test/resources/data/model/load/loadAlphaBeta.json");
        String newVariableDefinitionsJson = readFileAsString("src/test/resources/data/model/load/loadAlphaBeta_variable_definitions.json");

        cleanDB();

        // --- Put data first time with initial variable definitions --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get initial variable definitions
        List<ModelVariableDefinition> variableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        assertThat(variableDefinitions).hasSize(5);

        // --- Get initial variable definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + modelName + "/variables"))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> variableDefinitions1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(variableDefinitions1).hasSize(5);

        // --- Try to get variable definitions from unknown model --- //
        mvc.perform(get("/models/" + modelName + "_unknown" + "/variables"))
                .andExpect(status().isNotFound());

        // --- Put data second time which add only a variable definition --- //
        mvcResult = mvc.perform(post("/models/" + modelName + "/variables")
                        .content(newVariableDefinitionsJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions
        List<ModelVariableDefinition> variableDefinitions2 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Initial variable definitions = {}", variableDefinitions);
        LOGGER.info("Updated variable definitions = {}", variableDefinitions2);

        // check result
        // final model's variable definition must contain all ones of the initial model
        assertThat(variableDefinitions2.size() - variableDefinitions.size()).isOne();
        Assertions.assertThatDtoList(variableDefinitions2)
                .recursivelyContainAll(variableDefinitions);

        // --- Remove an existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/variables/remove")
                        .content(objectMapper.writeValueAsString(List.of(variableDefinitions2.get(5).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions
        List<ModelVariableDefinition> variableDefinitions3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Updated variable definitions = {}", variableDefinitions2);
        LOGGER.info("Removed variable definitions = {}", variableDefinitions3);

        // check result
        // final model's variable definition must contain all ones of model
        assertThat(variableDefinitions2.size() - variableDefinitions3.size()).isOne();
        Assertions.assertThatDtoList(variableDefinitions2).recursivelyContainAll(variableDefinitions3);

        // --- Remove all variable definitions --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/variables/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> variableDefinitions4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Unset variable definitions = {}", variableDefinitions4);

        // check result
        // must have no variable definition
        assertThat(variableDefinitions4).isEmpty();

        // --- Save new variable definition --- //
        List<ModelVariableDefinition> variableDefinitionList = objectMapper.readValue(newVariableDefinitionsJson, new TypeReference<>() { });
        variableDefinitionList.getFirst().setName("load_running_value_3");
        mvcResult = mvc.perform(post("/models/variables")
                        .content(objectMapper.writeValueAsString(variableDefinitionList))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> savedVariableDefinitionList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });

        // check result
        // must have the same number of input variable definitions
        assertThat(savedVariableDefinitionList).hasSameSizeAs(variableDefinitionList);

        // --- Add existing variable definition to model --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/variables/add")
                        .content(objectMapper.writeValueAsString(savedVariableDefinitionList.stream().map(ModelVariableDefinition::getName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions in the model
        List<ModelVariableDefinition> variableDefinitions6 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Updated variable definitions = {}", variableDefinitions6);
        // must have the same number of the above input variable definitions
        assertThat(variableDefinitions6).hasSameSizeAs(variableDefinitionList);

        // --- add unknown existing variable definition to model => must fail fast --- //
        mvc.perform(patch("/models/" + modelName + "/variables/add")
                        .content(objectMapper.writeValueAsString(List.of("variable_definition_unknown")))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // --- Delete definitively a variable definition --- //
        mvcResult = mvc.perform(delete("/models/variables")
                        .content(objectMapper.writeValueAsString(List.of(variableDefinitions.get(4).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> deletedVariableDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        LOGGER.info("Deleted variable definitions = {}", deletedVariableDefinitionNames);

        // Check result
        assertThat(deletedVariableDefinitionNames).hasSize(1);
        assertThat(deletedVariableDefinitionNames.getFirst()).isEqualTo(variableDefinitions.get(4).getName());

    }

    @Test
    public void testSaveNewVariablesSetThenModifyVariableDefinitions() throws Exception {
        String newVariablesSetJson = readFileAsString("src/test/resources/data/model/generator/variablesSet_generator2.json");
        String newVariableDefinitionsJson = readFileAsString("src/test/resources/data/model/generator/variablesSet_generator2_variable_definitions.json");

        cleanDB();

        // --- Put variables set first time with initial variable definitions --- //
        MvcResult mvcResult = mvc.perform(post("/models/variables-sets")
                        .content(newVariablesSetJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get initial variable definitions
        VariablesSet variablesSet = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions = variablesSet.getVariableDefinitions();

        // Check result
        assertThat(variableDefinitions).hasSize(2);

        // --- Put second time which add only a variable definition --- //
        mvcResult = mvc.perform(post("/models/variables-sets/" + variablesSet.getName() + "/variables")
                        .content(newVariableDefinitionsJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variable definitions after adding
        VariablesSet variablesSet2 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions2 = variablesSet2.getVariableDefinitions();
        LOGGER.info("Initial variable definitions = {}", variableDefinitions);
        LOGGER.info("Updated variable definitions = {}", variableDefinitions2);

        // Check result
        assertThat(variableDefinitions2).hasSize(4);
        // must contains all initial variable definitions
        Assertions.assertThatDtoList(variableDefinitions2).recursivelyContainAll(variableDefinitions);

        // check in the database, must be 4 variable definitions, 2 in the initial set and 2 added later
        mvcResult = mvc.perform(get("/models/variable-definitions/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> foundVariableDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundVariableDefinitionNames).hasSize(4);

        mvcResult = mvc.perform(get("/models/variable-definitions")
                        .param("variableDefinitionNames", String.join(",", foundVariableDefinitionNames))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> foundVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertThat(foundVariableDefinitions).hasSize(4);

        // --- Remove an existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/variables-sets/" + variablesSet.getName() + "/variables/remove")
                        .content(objectMapper.writeValueAsString(List.of(variableDefinitions2.get(3).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions
        VariablesSet variablesSet3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions3 = variablesSet3.getVariableDefinitions();
        LOGGER.info("Updated variable definitions = {}", variableDefinitions2);
        LOGGER.info("Removed variable definitions = {}", variableDefinitions3);

        // Check result
        assertThat(variableDefinitions3).hasSize(3);
        // must contains all variable definitions after removing
        Assertions.assertThatDtoList(variableDefinitions2).recursivelyContainAll(variableDefinitions3);

        // --- Remove all existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/variables-sets/" + variablesSet.getName() + "/variables/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions after remove all
        VariablesSet variablesSet4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions4 = variablesSet4.getVariableDefinitions();
        LOGGER.info("All removed variable definitions = {}", variableDefinitions4);

        // Check result
        assertThat(variableDefinitions4).isEmpty();

    }

    @Test
    public void testSaveNewVariablesSetsWhichShareVariableDefinitions() throws Exception {
        String newVariablesSetJson = readFileAsString("src/test/resources/data/model/generator/variablesSet_ThreeWindingsSynchronousGenerator.json");
        String newVariablesSet2Json = readFileAsString("src/test/resources/data/model/generator/variablesSet_FourWindingsSynchronousGenerator.json");

        cleanDB();

        // --- Put the first variables set with 2 variable definitions --- //
        MvcResult mvcResult = mvc.perform(post("/models/variables-sets")
                        .content(newVariablesSetJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get initial variable definitions
        VariablesSet threeWindingVariablesSet = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);

        // --- Put the second variables set with 3 variable definitions in which 2 ones are identical to first variables set --- //
        mvcResult = mvc.perform(post("/models/variables-sets")
                        .content(newVariablesSet2Json)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get initial variable definitions
        VariablesSet fourWindingVariablesSet = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);

        // --- Get initial variable definition from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/variables-sets/" + threeWindingVariablesSet.getName() + "/variables"))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> threeWindingVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(threeWindingVariableDefinitions).hasSize(2);

        // --- Try to get variable definition from unknown variables set --- //
        mvc.perform(get("/models/variables-sets/" + "variable_set_unknown" + "/variables"))
                .andExpect(status().isNotFound());

        // --- Get initial variable definition from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/variables-sets/" + fourWindingVariablesSet.getName() + "/variables"))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> fourWindingVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(fourWindingVariableDefinitions).hasSize(3);

        // cross-check between two variables set
        Sets.SetView<ModelVariableDefinition> intersectionVariableDefinitions = Sets.intersection(new HashSet<>(fourWindingVariableDefinitions), new HashSet<>(threeWindingVariableDefinitions));
        assertThat(intersectionVariableDefinitions).hasSize(2);

        // check in the database, must be 3 variable definitions, i.e. 2 shared variable definitions
        // and 1 in FourWindingsSynchronousGenerator
        mvcResult = mvc.perform(get("/models/variable-definitions/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> foundVariableDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundVariableDefinitionNames).hasSize(3);

        mvcResult = mvc.perform(get("/models/variable-definitions")
                        .param("variableDefinitionNames", String.join(",", foundVariableDefinitionNames))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> foundVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundVariableDefinitions).hasSize(3);

    }

    @Test
    public void testSaveNewLoadModelsWhichShareParameterDefinitionsAndVariableDefinitions() throws Exception {
        String newLoadAlphaBetaModelJson = readFileAsString("src/test/resources/data/model/load/loadAlphaBeta.json");
        String newLoadPQModelJson = readFileAsString("src/test/resources/data/model/load/loadPQ.json");

        cleanDB();

        // *** LOAD ALPHA BETA *** //
        // --- Put data first time with initial parameter/variable definitions --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newLoadAlphaBetaModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        String loadAlphaBetaModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // *** LOAD PQ *** //
        // --- Put data first time with initial parameter/variable definitions --- //
        mvcResult = mvc.perform(post("/models/")
                        .content(newLoadPQModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        String loadPQModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Get variable definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadAlphaBetaModelName + "/variables"))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> loadAlphaBetaVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(loadAlphaBetaVariableDefinitions).hasSize(5);

        // --- Get initial parameter definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadAlphaBetaModelName + "/parameters/definitions"))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> loadAlphaBetaParameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(loadAlphaBetaParameterDefinitions).hasSize(6);

        // --- Get variable definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadPQModelName + "/variables"))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> loadPQVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(loadPQVariableDefinitions).hasSize(3);

        // --- Get initial parameter definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadPQModelName + "/parameters/definitions"))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> loadPQParameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(loadPQParameterDefinitions).hasSize(4);

        // cross-check variable definitions between two models
        Sets.SetView<ModelVariableDefinition> intersectionVariableDefinitions = Sets.intersection(new HashSet<>(loadAlphaBetaVariableDefinitions), new HashSet<>(loadPQVariableDefinitions));
        assertThat(intersectionVariableDefinitions).hasSize(3);

        // cross-check parameter definitions between two models
        Sets.SetView<ModelParameterDefinition> intersectionParameterDefinitions = Sets.intersection(new HashSet<>(loadAlphaBetaParameterDefinitions), new HashSet<>(loadPQParameterDefinitions));
        assertThat(intersectionParameterDefinitions).hasSize(3);

        // the last parameter definition of load alpha beta model must be NETWORK
        ModelParameterDefinition lastParameterDefinitionInLoadAlphaBetaModel = loadAlphaBetaParameterDefinitions.stream().reduce((first, second) -> second).get();
        assertThat(lastParameterDefinitionInLoadAlphaBetaModel.getOrigin()).isEqualTo(ParameterOrigin.NETWORK);

        // the last parameter definition of load PQ model must be USER
        ModelParameterDefinition lastParameterDefinitionInLoadPQModel = loadPQParameterDefinitions.stream().reduce((first, second) -> second).get();
        assertThat(lastParameterDefinitionInLoadPQModel.getOrigin()).isEqualTo(ParameterOrigin.USER);

        // two last parameter definitions in two models must be the same name, type, originName and fixedValue
        assertThat(lastParameterDefinitionInLoadPQModel.getName()).isEqualTo(lastParameterDefinitionInLoadAlphaBetaModel.getName());
        assertThat(lastParameterDefinitionInLoadPQModel.getType()).isEqualTo(lastParameterDefinitionInLoadAlphaBetaModel.getType());
        assertThat(lastParameterDefinitionInLoadPQModel.getOriginName()).isEqualTo(lastParameterDefinitionInLoadAlphaBetaModel.getOriginName());
        assertThat(lastParameterDefinitionInLoadPQModel.getFixedValue()).isEqualTo(lastParameterDefinitionInLoadAlphaBetaModel.getFixedValue());

    }

    @Test
    @Transactional
    public void testSaveGeneratorModel() throws Exception {
        String modelName = "GeneratorSynchronousThreeWindingsProportionalRegulations";
        String newModelJson = readFileAsString("src/test/resources/data/model/generator/generatorSynchronousThreeWindingsProportionalRegulations.json");

        // Put data
        mvc.perform(post("/models/")
                        .content(newModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Get Data
        ModelEntity savedModel = modelRepository.findByModelName(modelName).orElseThrow();

        // sanity check
        assertThat(savedModel.getModelName()).isEqualTo(modelName);
        assertThat(savedModel.getEquipmentType()).isEqualTo(EquipmentType.GENERATOR);

        // check variables sets
        assertThat(savedModel.getVariableSets()).hasSize(2);
    }

    @Test
    public void testSaveGeneratorModelThenModifyVariablesSets() throws Exception {
        String newVariablesSetJson = readFileAsString("src/test/resources/data/model/generator/variablesSet_generator2.json");
        String newModelJson = readFileAsString("src/test/resources/data/model/generator/generatorSynchronousThreeWindingsProportionalRegulations.json");

        cleanDB();

        // --- Put first time with initial variables sets --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets and variable definitions in each set
        Model model = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet = model.getVariablesSets();
        // get variable definitions of sets
        List<ModelVariableDefinition> variableDefinitionsOfGeneratorSet = variablesSet.stream().filter(set -> "Generator".equals(set.getName())).findFirst().orElseThrow().getVariableDefinitions();
        List<ModelVariableDefinition> variableDefinitionsOfVoltageRegulatorSet = variablesSet.stream().filter(set -> "VoltageRegulator".equals(set.getName())).findFirst().orElseThrow().getVariableDefinitions();

        // Check result
        assertThat(variablesSet).hasSize(2);
        assertThat(variableDefinitionsOfGeneratorSet).hasSize(4);
        assertThat(variableDefinitionsOfVoltageRegulatorSet.size()).isOne();

        // --- Get initial variable sets from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + model.getModelName() + "/variables-sets"))
                .andExpect(status().isOk()).andReturn();
        List<VariablesSet> variablesSet1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(variablesSet1).hasSize(2);

        // --- Try to get variable sets from unknown model --- //
        mvc.perform(get("/models/" + model.getModelName() + "_unknown" + "/variables"))
                .andExpect(status().isNotFound());

        // --- Put second time which add only a variables sets --- //
        mvcResult = mvc.perform(post("/models/" + model.getModelName() + "/variables-sets")
                        .content("[\n" + newVariablesSetJson + "\n]")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets and variable definitions in each set
        Model model2 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet2 = model2.getVariablesSets();
        // get variable definitions of sets
        variableDefinitionsOfGeneratorSet = variablesSet2.stream().filter(set -> "Generator".equals(set.getName())).findFirst().orElseThrow().getVariableDefinitions();
        variableDefinitionsOfVoltageRegulatorSet = variablesSet2.stream().filter(set -> "VoltageRegulator".equals(set.getName())).findFirst().orElseThrow().getVariableDefinitions();
        List<ModelVariableDefinition> variableDefinitionsOfRegulator2Set = variablesSet2.stream().filter(set -> "Generator2".equals(set.getName())).findFirst().orElseThrow().getVariableDefinitions();

        // Check result
        assertThat(variablesSet2).hasSize(3);
        assertThat(variableDefinitionsOfGeneratorSet).hasSize(4);
        assertThat(variableDefinitionsOfVoltageRegulatorSet.size()).isOne();
        assertThat(variableDefinitionsOfRegulator2Set).hasSize(2);

        // --- Remove an existing variables set --- //
        mvcResult = mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/remove")
                        .content(objectMapper.writeValueAsString(List.of(variablesSet2.get(2).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets
        Model model3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet3 = model3.getVariablesSets();

        // Check result
        assertThat(variablesSet3).hasSize(2);
        // must contains all variables sets after removing
        Assertions.assertThatDtoList(variablesSet2).recursivelyContainAll(variablesSet3);

        // --- Remove all variables set --- //
        mvcResult = mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets
        Model model4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet4 = model4.getVariablesSets();

        // Check result
        assertThat(variablesSet4).isEmpty();

        // --- Add an existing variables set --- //
        mvcResult = mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/add")
                        .content(objectMapper.writeValueAsString(List.of(variablesSet2.get(2).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets
        Model model5 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet5 = model5.getVariablesSets();

        // Check result
        assertThat(variablesSet5.size()).isOne();

        // --- Add an unknown existing variables set => must fail fast --- //
        mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/add")
                        .content(objectMapper.writeValueAsString(List.of("variable_set_unknown")))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // --- Delete definitively a variables set --- //
        mvcResult = mvc.perform(delete("/models/variables-sets")
                        .content(objectMapper.writeValueAsString(List.of(variablesSet2.get(2).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> deletedVariablesSetNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });

        // Check result
        assertThat(deletedVariablesSetNames.size()).isOne();
        assertThat(deletedVariablesSetNames.getFirst()).isEqualTo(variablesSet2.get(2).getName());

        // -- there is 2 variables set in database, i.e. two initial, one added, one deleted => remain 2 -- //
        mvcResult = mvc.perform(get("/models/variables-sets/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> foundVariableSetNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundVariableSetNames).hasSize(2);

        mvcResult = mvc.perform(get("/models/variables-sets")
                        .param("variablesSetNames", String.join(",", foundVariableSetNames))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<VariablesSet> foundVariableSets = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        assertThat(foundVariableSets).hasSize(2);
    }

    @Test
    @Transactional
    public void deleteTest() throws Exception {

        String name = "setName";
        String modelName = "LoadAlphaBeta";
        String set = """
                {
                  "name": "%s",
                  "parameters": [
                    {
                      "name": "load_alpha",
                      "value": "1.5"
                    },
                    {
                      "name": "load_beta",
                      "value": "2.5"
                    }
                  ]
                }
            """
            .formatted(name);
        String setGroup = """
                {
                  "name": "%s",
                  "modelName": "%s",
                  "type": "FIXED",
                  "sets": [
                    %s
                  ]
                }
            """
            .formatted(name, modelName, set);

        // Put data
        mvc.perform(post("/models/" + modelName + "/parameters/sets/strict")
                        .content(setGroup)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        MvcResult mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        String setJson = mvcResult.getResponse().getContentAsString();
        List<ParametersSet> resultSetList = objectMapper.readValue(setJson, new TypeReference<>() { });
        ParametersSet expectedSet = objectMapper.readValue(set, ParametersSet.class);

        Assertions.assertThat(resultSetList.getFirst()).recursivelyEquals(expectedSet);

        // Trying to delete from a non-existing model will throw
        mvc.perform(delete("/models/" + "unknownModel" + "/parameters/sets/" + name + "/" + "FIXED" + "/" + name))
                .andExpect(status().isNotFound());

        // Trying to delete from a non-existing setGroup will throw
        mvc.perform(delete("/models/" + modelName + "/parameters/sets/" + "unknownGroup" + "/" + "FIXED" + "/" + name))
                .andExpect(status().isNotFound());

        // Trying to delete a non-existing set will do nothing
        mvc.perform(delete("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED" + "/" + "unusedName"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(setGroup));

        mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        setJson = mvcResult.getResponse().getContentAsString();
        resultSetList = objectMapper.readValue(setJson, new TypeReference<>() { });
        Assertions.assertThat(resultSetList.getFirst()).recursivelyEquals(expectedSet);

        // Delete the set
        mvc.perform(delete("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED" + "/" + name))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("""
                    {
                      "name": "%s",
                      "modelName": "%s",
                      "type": "FIXED",
                      "sets": []
                    }
                """
                .formatted(name, modelName)));
        mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();
        List<ParametersSet> resultSets = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() { });
        Assertions.assertThat(resultSets).isEmpty();
    }

    @Test
    public void testDeleteLoadModelsWhichShareParameterDefinitionsAndVariableDefinitions() throws Exception {

        // These model share 4 parameter definitions and 3 variable definitions
        String newLoadAlphaBetaModelJson = readFileAsString("src/test/resources/data/model/load/loadAlphaBeta.json");
        String newLoadPQModelJson = readFileAsString("src/test/resources/data/model/load/loadPQ.json");

        cleanDB();

        // *** LOAD ALPHA BETA *** //
        // --- Put data first time with initial parameter/variable definitions --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newLoadAlphaBetaModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        String loadAlphaBetaModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // *** LOAD PQ *** //
        // --- Put data first time with initial parameter/variable definitions --- //
        mvcResult = mvc.perform(post("/models/")
                        .content(newLoadPQModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        String loadPQModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Delete LOAD ALPHA BETA --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(loadAlphaBetaModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model LOAD ALPHA BETA must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findByModelName(loadAlphaBetaModelName);
        assertThat(foundNoneExistingModelOpt).isNotPresent();

        // must delete only 2 parameter definitions which are only used by load alpha beta
        // the rest 4 shared parameter definitions must be always present in db
        List<ModelParameterDefinitionEntity> foundNoneExistingParameterDefinitions = modelParameterDefinitionRepository.findAllByNameIn(List.of("load_alpha", "load_beta"));
        assertThat(foundNoneExistingParameterDefinitions).isEmpty();
        List<ModelParameterDefinitionEntity> foundExistingParameterDefinitions = modelParameterDefinitionRepository.findAllByNameIn(List.of("load_P0Pu", "load_Q0Pu", "load_U0Pu", "load_UPhase0"));
        assertThat(foundExistingParameterDefinitions).hasSize(4);

        // must delete only 2 variable definitions which are only used by load alpha beta
        // the rest 3 shared variable definitions must be always present in db
        List<ModelVariableDefinitionEntity> foundNoneExistingVariableDefinitions = modelVariableRepository.findAllByNameIn(List.of("load_PRefPu", "load_running_value"));
        assertThat(foundNoneExistingVariableDefinitions).isEmpty();
        List<ModelVariableDefinitionEntity> foundExistingVariableDefinitions = modelVariableRepository.findAllByNameIn(List.of("load_PPu", "load_QPu", "load_QRefPu"));
        assertThat(foundExistingVariableDefinitions).hasSize(3);

        // --- Delete LOAD PQ --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(loadPQModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model LOAD PQ must be not exist in db
        foundNoneExistingModelOpt = modelRepository.findByModelName(loadPQModelName);
        assertThat(foundNoneExistingModelOpt).isNotPresent();

        // db must not contain any parameter definition
        assertThat(modelParameterDefinitionRepository.findAll()).isEmpty();
        // db must not contain any variable definition
        assertThat(modelVariableRepository.findAll()).isEmpty();

    }

    @Test
    public void testDeleteGeneratorModelsWithVariableSets() throws Exception {
        String newGeneratorModelJson = readFileAsString("src/test/resources/data/model/generator/generatorSynchronousThreeWindingsProportionalRegulations.json");

        cleanDB();

        // --- Put first time with initial variables sets --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Check result --- //
        // This model has two variable sets => must be present in the db
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("Generator", "VoltageRegulator"));
        assertThat(variableSets).hasSize(2);
        // Variable set Generator contains 4 variable definitions and VoltageRegulator contains 1 variable definition => total = 5
        assertThat(modelVariableRepository.findAll()).hasSize(5);

        // --- Delete model generator --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model generator model must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findByModelName(generatorModelName);
        assertThat(foundNoneExistingModelOpt).isNotPresent();

        // db must not contain any variable set
        assertThat(modelVariablesSetRepository.findAll()).isEmpty();
        // db must not contain any variable definition
        assertThat(modelVariableRepository.findAll()).isEmpty();
    }

    @Test
    public void testDeleteGeneratorModelsWhichShareVariableSets() throws Exception {
        String newGeneratorThreeWindingsModelJson = readFileAsString("src/test/resources/data/model/generator/generatorSynchronousThreeWindingsProportionalRegulations.json");
        String newGeneratorFourWindingsModelJson = readFileAsString("src/test/resources/data/model/generator/generatorSynchronousFourWindingsProportionalRegulations.json");

        cleanDB();

        // --- Put first time with initial variables sets for Three Windings Generator --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorThreeWindingsModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorModelThreeWindingsName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Put first time with initial variables sets for Three Windings Generator --- //
        mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorFourWindingsModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorModelFourWindingsName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Check result --- //
        // These models have two shared variable sets => must be present in the db
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("Generator", "VoltageRegulator"));
        assertThat(variableSets).hasSize(2);
        // Variable set Generator contains 4 variable definitions and VoltageRegulator contains 1 variable definition => total = 5
        assertThat(modelVariableRepository.findAll()).hasSize(5);

        // --- Delete model Three Windings Generator --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorModelThreeWindingsName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Three Windings Generator must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findByModelName(generatorModelThreeWindingsName);
        assertThat(foundNoneExistingModelOpt).isNotPresent();

        // These models have two shared variable sets => after delete one model, shared variable sets must be always in db
        variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("Generator", "VoltageRegulator"));
        assertThat(variableSets).hasSize(2);
        // Variable set Generator contains 4 variable definitions and VoltageRegulator contains 1 variable definition => total = 5
        assertThat(modelVariableRepository.findAll()).hasSize(5);

        // --- Delete model Four Windings Generator --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorModelFourWindingsName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Three Windings Generator must be not exist in db
        foundNoneExistingModelOpt = modelRepository.findByModelName(generatorModelFourWindingsName);
        assertThat(foundNoneExistingModelOpt).isNotPresent();

        // the last model which uses shared variable sets has been deleted
        // db must not contain any variable set
        assertThat(modelVariablesSetRepository.findAll()).isEmpty();
        // db must not contain any variable definition
        assertThat(modelVariableRepository.findAll()).isEmpty();

    }

    @Test
    public void testDeleteGeneratorModelsWhichShareVariableDefinitionsBetweenDifferentVariableSets() throws Exception {
        String newGeneratorPQModelJson = readFileAsString("src/test/resources/data/model/generator/generatorPQ.json");
        String newGeneratorPVModelJson = readFileAsString("src/test/resources/data/model/generator/generatorPV.json");

        cleanDB();

        // --- Put first time with initial variables sets for PQ Generator --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorPQModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorPQModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Put first time with initial variables sets for PV Generator --- //
        mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorPVModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorPVModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Check result --- //
        // These models have two shared 3 variables definitions between 2 different variable sets => must be present in the db
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("GeneratorPQ", "GeneratorPV"));
        assertThat(variableSets).hasSize(2);
        // Variable set GeneratorPQ and GeneratorPV share 3 variable definitions
        assertThat(modelVariableRepository.findAll()).hasSize(3);

        // --- Delete model Generator PQ --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorPQModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Generator PQ must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findByModelName(generatorPQModelName);
        assertThat(foundNoneExistingModelOpt).isNotPresent();

        // Variable set Generator PQ must be not exist in db
        variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("GeneratorPQ"));
        assertThat(variableSets).isEmpty();
        // 3 variable definitions used by variable set GeneratorPV must be always present in db
        assertThat(modelVariableRepository.findAll()).hasSize(3);

        // --- Delete model Generator PV --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorPVModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Variable set Generator PV must be not exist in db
        variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("GeneratorPV"));
        assertThat(variableSets).isEmpty();
        // 3 variable definitions used by variable set GeneratorPV must be not exist in db
        assertThat(modelVariableRepository.findAll()).isEmpty();
    }

    @Test
    public void testDeleteAllGeneratorModelsWhichShareVariableDefinitionsBetweenDifferentVariableSets() throws Exception {
        String newGeneratorPQModelJson = readFileAsString("src/test/resources/data/model/generator/generatorPQ.json");
        String newGeneratorPVModelJson = readFileAsString("src/test/resources/data/model/generator/generatorPV.json");

        cleanDB();

        // --- Put first time with initial variables sets for PQ Generator --- //
        MvcResult mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorPQModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorPQModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Put first time with initial variables sets for PV Generator --- //
        mvcResult = mvc.perform(post("/models/")
                        .content(newGeneratorPVModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        String generatorPVModelName = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getModelName();

        // --- Check result --- //
        // These models have two shared 3 variables definitions between 2 different variable sets => must be present in the db
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("GeneratorPQ", "GeneratorPV"));
        assertThat(variableSets).hasSize(2);
        // Variable set GeneratorPQ and GeneratorPV share 3 variable definitions
        List<ModelVariableDefinitionEntity> variables = modelVariableRepository.findAll();
        assertThat(variables).hasSize(3);

        // --- Delete model Generator PQ --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorPQModelName, generatorPVModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Generator PQ/PV must be not exist in db
        List<ModelEntity> modelEntities = modelRepository.findAllByModelNameIn(List.of(generatorPQModelName, generatorPVModelName));
        assertThat(modelEntities).isEmpty();

        // Variable set Generator PQ/PV must be not exist in db
        variableSets = modelVariablesSetRepository.findAllByNameIn(List.of("GeneratorPV", "GeneratorPQ"));
        assertThat(variableSets).isEmpty();
        // 3 variable definitions used by variable sets GeneratorPQ/PV must be not exist in db
        assertThat(modelVariableRepository.findAll()).isEmpty();
    }
}
