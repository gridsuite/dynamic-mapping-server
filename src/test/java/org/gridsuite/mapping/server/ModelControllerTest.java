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
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    public static Logger LOGGER = LoggerFactory.getLogger(ModelControllerTest.class);

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
        return new ModelParameterDefinitionEntity(new ModelParameterDefinition(name, type, null, null, null));
    }

    @Before
    public void setUp() {
        cleanDB();

        // prepare token model
        ModelEntity modelToSave = new ModelEntity("LoadAlphaBeta", EquipmentType.LOAD, false,
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

        LOGGER.info("Automatons result in Json array = \n" + automatonsJsonResult);

        // Check result
        JsonNode jsonNode = objectMapper.readTree(automatonsJsonResult);
        assertEquals(true, jsonNode.isArray());
        assertEquals(3, jsonNode.size());
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

        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + set + "]", true));

        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "PREFIX")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Date setCreationDate = modelRepository.findById(modelName).get().getSetsGroups().get(0).getSets().get(0).getLastModifiedDate();

        // Update data
        mvc.perform(post("/models/" + modelName + "/parameters/sets/strict")
                        .content(setGroup)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        Date setUpdateDate = modelRepository.findById(modelName).get().getSetsGroups().get(0).getSets().get(0).getLastModifiedDate();

        assertThat(setCreationDate.compareTo(setUpdateDate) < 0);
    }

    @Test
    public void definitionTest() throws Exception {
        String modelName = "LoadAlphaBeta";

        mvc.perform(get("/models/" + modelName + "/parameters/definitions")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("""
                    [
                        {"name": "load_alpha", "type": "DOUBLE", "origin": "USER", "originName": null, "fixedValue": null},
                        {"name": "load_beta", "type": "DOUBLE", "origin": "USER", "originName": null, "fixedValue": null},
                        {"name": "load_P0Pu", "type": "DOUBLE", "origin": "NETWORK", "originName": "p_pu", "fixedValue": null},
                        {"name": "load_Q0Pu", "type": "DOUBLE", "origin": "NETWORK", "originName": "q_pu", "fixedValue": null},
                        {"name": "load_U0Pu", "type": "DOUBLE", "origin": "NETWORK", "originName": "v_pu", "fixedValue": null},
                        {"name": "load_UPhase0", "type": "DOUBLE", "origin": "NETWORK", "originName": "angle_pu", "fixedValue": null}
                    ]
                """, false));

        // --- GET all names of parameter definitions in the database --- //
        MvcResult mvcResult = mvc.perform(get("/models/parameter-definitions/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        // must contain 6 names of parameter definitions in the database
        List<String> foundParameterDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<String>>() { });
        assertEquals(6, foundParameterDefinitionNames.size());

        // --- GET all parameter definitions in the database --- //
        mvcResult = mvc.perform(get("/models/parameter-definitions")
                        .contentType(APPLICATION_JSON)
                        .param("parameterDefinitionNames", foundParameterDefinitionNames.stream().collect(Collectors.joining(","))))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        // must contain 6 parameter definitions in the database
        List<ModelParameterDefinition> foundParameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<List<ModelParameterDefinition>>() { });
        assertEquals(6, foundParameterDefinitions.size());
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
        ModelEntity loadModel = modelRepository.findById("LoadAlphaBeta").get();
        List<ModelSetsGroupEntity> loadGroups = loadModel.getSetsGroups();
        ModelSetsGroupEntity loadGroup = new ModelSetsGroupEntity("LAB", loadModel.getModelName(), null, SetGroupType.FIXED, loadModel);
        ArrayList<ModelParameterSetEntity> groupSets = new ArrayList<>();
        ModelParameterSetEntity setToSave = new ModelParameterSetEntity("LAB", loadGroup.getName(), loadModel.getModelName(), loadGroup.getType(),
                null,
                new Date(),
                loadGroup);
        ArrayList<ModelParameterEntity> setParameters = new ArrayList<>();
        setParameters.add(new ModelParameterEntity("load_alpha", loadGroup.getModelName(), loadGroup.getName(), loadGroup.getType(), setToSave.getName(), "1.5", setToSave));
        setParameters.add(new ModelParameterEntity("load_beta", loadGroup.getModelName(), loadGroup.getName(), loadGroup.getType(), setToSave.getName(), "2.5", setToSave));
        setToSave.setParameters(setParameters);
        groupSets.add(setToSave);
        loadGroup.setSets(groupSets);
        loadGroups.add(loadGroup);
        loadModel.setSetsGroups(loadGroups);
        modelRepository.save(loadModel);

        ModelEntity generatorThreeModel = new ModelEntity("GeneratorThreeWindings", EquipmentType.GENERATOR, false, List.of(), null, Set.of(), Set.of(), null, null);
        ArrayList<ModelSetsGroupEntity> generatorThreeGroups = new ArrayList<>();
        generatorThreeGroups.add(new ModelSetsGroupEntity("GSTWPR", generatorThreeModel.getModelName(), new ArrayList<>(), SetGroupType.PREFIX, generatorThreeModel));
        generatorThreeModel.setSetsGroups(generatorThreeGroups);
        modelRepository.save(generatorThreeModel);

        mvc.perform(get("/models/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("""
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
                """, true));

        // --- GET parameter set groups in the database --- //
        MvcResult mvcResult = mvc.perform(get("/models/parameters-sets-groups")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        // check result
        // must contain 2 parameter set groups, i.e. "LAB" and "GSTWPR"
        List<ParametersSetsGroup> foundParametersSetsGroups = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ParametersSetsGroup>>() { });
        assertEquals(2, foundParametersSetsGroups.size());

        // --- GET parameter sets in the database --- //
        mvcResult = mvc.perform(get("/models/parameters-sets")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        // check result
        // must contain only 1 parameter set, i.e. "LAB"
        List<ParametersSet> foundParametersSets = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ParametersSet>>() { });
        assertEquals(1, foundParametersSets.size());

        // --- GET parameters in the database --- //
        mvcResult = mvc.perform(get("/models/parameters")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();

        // check result
        // must contain 2 parameter, i.e. "load_alpha", "load_beta"
        List<ModelParameter> foundParameters = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelParameter>>() { });
        assertEquals(2, foundParameters.size());
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
        assertEquals(6, parameterDefinitions.size());

        // --- Get initial parameter definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + modelName + "/parameters/definitions"))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> parameterDefinitions1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelParameterDefinition>>() { });
        assertEquals(6, parameterDefinitions1.size());

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
        LOGGER.info("Initial parameter definitions = " + parameterDefinitions);
        LOGGER.info("Updated parameter definitions = " + parameterDefinitions2);

        // check result
        // final model's parameter definitions must contain all ones of initial model
        assertEquals(1, parameterDefinitions2.size() - parameterDefinitions.size());
        assertTrue(parameterDefinitions2.containsAll(parameterDefinitions));

        // --- Remove an existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/parameters/definitions/remove")
                        .content(objectMapper.writeValueAsString(List.of(parameterDefinitions2.get(5).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current parameter definitions
        List<ModelParameterDefinition> parameterDefinitions3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Updated parameter definitions = " + parameterDefinitions2);
        LOGGER.info("Removed parameter definitions = " + parameterDefinitions3);

        // check result
        // final model's parameter definitions must contain all ones of model
        assertEquals(1, parameterDefinitions2.size() - parameterDefinitions3.size());
        assertTrue(parameterDefinitions2.containsAll(parameterDefinitions3));

        // --- Remove all parameter definitions --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/parameters/definitions/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelParameterDefinition> parameterDefinitions4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Unset parameter definitions = " + parameterDefinitions4);

        // check result
        // must have no parameter definition
        assertEquals(0, parameterDefinitions4.size());

        // --- Save new parameter definition --- //
        List<ModelParameterDefinition> parameterDefinitionList = objectMapper.readValue(newParameterDefinitionsJson, new TypeReference<List<ModelParameterDefinition>>() { });
        parameterDefinitionList.get(0).setName("load_UPhase0_3");
        mvcResult = mvc.perform(post("/models/parameters/definitions")
                        .content(objectMapper.writeValueAsString(parameterDefinitionList))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> savedParameterDefinitionList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelParameterDefinition>>() { });

        // check result
        // must have the same number of input variable definitions
        assertEquals(parameterDefinitionList.size(), savedParameterDefinitionList.size());

        // --- Add existing parameter definition to model --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/parameters/definitions/add?origin=USER")
                        .content(objectMapper.writeValueAsString(savedParameterDefinitionList.stream().map(ModelParameterDefinition::getName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current parameter definitions in the model
        List<ModelParameterDefinition> parameterDefinitions6 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getParameterDefinitions();
        LOGGER.info("Updated parameter definitions = " + parameterDefinitions6);
        // must have the same number of above input parameter definitions
        assertEquals(savedParameterDefinitionList.size(), parameterDefinitions6.size());

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
        List<String> deletedParameterDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<String>>() { });
        LOGGER.info("Deleted parameter definitions = " + deletedParameterDefinitionNames);

        // Check result
        assertEquals(1, deletedParameterDefinitionNames.size());
        assertEquals(parameterDefinitions.get(4).getName(), deletedParameterDefinitionNames.get(0));
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
        assertEquals(5, variableDefinitions.size());

        // --- Get initial variable definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + modelName + "/variables"))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> variableDefinitions1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(5, variableDefinitions1.size());

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
        LOGGER.info("Initial variable definitions = " + variableDefinitions);
        LOGGER.info("Updated variable definitions = " + variableDefinitions2);

        // check result
        // final model's variable definition must contains all ones of initial model
        assertEquals(1, variableDefinitions2.size() - variableDefinitions.size());
        assertTrue(variableDefinitions2.containsAll(variableDefinitions));

        // --- Remove an existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/variables/remove")
                        .content(objectMapper.writeValueAsString(List.of(variableDefinitions2.get(5).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions
        List<ModelVariableDefinition> variableDefinitions3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Updated variable definitions = " + variableDefinitions2);
        LOGGER.info("Removed variable definitions = " + variableDefinitions3);

        // check result
        // final model's variable definition must contain all ones of model
        assertEquals(1, variableDefinitions2.size() - variableDefinitions3.size());
        assertTrue(variableDefinitions2.containsAll(variableDefinitions3));

        // --- Remove all variable definitions --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/variables/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> variableDefinitions4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Unset variable definitions = " + variableDefinitions4);

        // check result
        // must have no variable definition
        assertEquals(0, variableDefinitions4.size());

        // --- Save new variable definition --- //
        List<ModelVariableDefinition> variableDefinitionList = objectMapper.readValue(newVariableDefinitionsJson, new TypeReference<List<ModelVariableDefinition>>() { });
        variableDefinitionList.get(0).setName("load_running_value_3");
        mvcResult = mvc.perform(post("/models/variables")
                        .content(objectMapper.writeValueAsString(variableDefinitionList))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> savedVariableDefinitionList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });

        // check result
        // must have the same number of input variable definitions
        assertEquals(variableDefinitionList.size(), savedVariableDefinitionList.size());

        // --- Add existing variable definition to model --- //
        mvcResult = mvc.perform(patch("/models/" + modelName + "/variables/add")
                        .content(objectMapper.writeValueAsString(savedVariableDefinitionList.stream().map(ModelVariableDefinition::getName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions in the model
        List<ModelVariableDefinition> variableDefinitions6 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class).getVariableDefinitions();
        LOGGER.info("Updated variable definitions = " + variableDefinitions6);
        // must have the same number of above input variable definitions
        assertEquals(variableDefinitionList.size(), variableDefinitions6.size());

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
        List<String> deletedVariableDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<String>>() { });
        LOGGER.info("Deleted variable definitions = " + deletedVariableDefinitionNames);

        // Check result
        assertEquals(1, deletedVariableDefinitionNames.size());
        assertEquals(variableDefinitions.get(4).getName(), deletedVariableDefinitionNames.get(0));

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
        assertEquals(2, variableDefinitions.size());

        // --- Put second time which add only a variable definition --- //
        mvcResult = mvc.perform(post("/models/variables-sets/" + variablesSet.getName() + "/variables")
                        .content(newVariableDefinitionsJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variable definitions after adding
        VariablesSet variablesSet2 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions2 = variablesSet2.getVariableDefinitions();
        LOGGER.info("Initial variable definitions = " + variableDefinitions);
        LOGGER.info("Updated variable definitions = " + variableDefinitions2);

        // Check result
        assertEquals(4, variableDefinitions2.size());
        // must contains all initial variable definitions
        assertTrue(variableDefinitions2.containsAll(variableDefinitions));

        // check in the database, must be 4 variable definitions, 2 in the initial set and 2 added later
        mvcResult = mvc.perform(get("/models/variable-definitions/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> foundVariableDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<String>>() { });
        assertEquals(4, foundVariableDefinitionNames.size());

        mvcResult = mvc.perform(get("/models/variable-definitions")
                        .param("variableDefinitionNames", foundVariableDefinitionNames.stream().collect(Collectors.joining(",")))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> foundVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(4, foundVariableDefinitions.size());

        // --- Remove an existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/variables-sets/" + variablesSet.getName() + "/variables/remove")
                        .content(objectMapper.writeValueAsString(List.of(variableDefinitions2.get(3).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions
        VariablesSet variablesSet3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions3 = variablesSet3.getVariableDefinitions();
        LOGGER.info("Updated variable definitions = " + variableDefinitions2);
        LOGGER.info("Removed variable definitions = " + variableDefinitions3);

        // Check result
        assertEquals(3, variableDefinitions3.size());
        // must contains all variable definitions after removing
        assertTrue(variableDefinitions2.containsAll(variableDefinitions3));

        // --- Remove all existing variable definition --- //
        mvcResult = mvc.perform(patch("/models/variables-sets/" + variablesSet.getName() + "/variables/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get current variable definitions after remove all
        VariablesSet variablesSet4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), VariablesSet.class);
        List<ModelVariableDefinition> variableDefinitions4 = variablesSet4.getVariableDefinitions();
        LOGGER.info("All removed variable definitions = " + variableDefinitions4);

        // Check result
        assertEquals(0, variableDefinitions4.size());

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
        List<ModelVariableDefinition> threeWindingVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(2, threeWindingVariableDefinitions.size());

        // --- Try to get variable definition from unknown variables set --- //
        mvc.perform(get("/models/variables-sets/" + "variable_set_unknown" + "/variables"))
                .andExpect(status().isNotFound());

        // --- Get initial variable definition from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/variables-sets/" + fourWindingVariablesSet.getName() + "/variables"))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> fourWindingVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(3, fourWindingVariableDefinitions.size());

        // cross-check between two variables set
        Sets.SetView<ModelVariableDefinition> intersectionVariableDefinitions = Sets.intersection(new HashSet<>(fourWindingVariableDefinitions), new HashSet<>(threeWindingVariableDefinitions));
        assertEquals(2, intersectionVariableDefinitions.size());

        // check in the database, must be 3 variable definitions, i.e. 2 shared variable definitions
        // and 1 in FourWindingsSynchronousGenerator
        mvcResult = mvc.perform(get("/models/variable-definitions/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> foundVariableDefinitionNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<String>>() { });
        assertEquals(3, foundVariableDefinitionNames.size());

        mvcResult = mvc.perform(get("/models/variable-definitions")
                        .param("variableDefinitionNames", foundVariableDefinitionNames.stream().collect(Collectors.joining(",")))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<ModelVariableDefinition> foundVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(3, foundVariableDefinitions.size());

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
        List<ModelVariableDefinition> loadAlphaBetaVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(5, loadAlphaBetaVariableDefinitions.size());

        // --- Get initial parameter definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadAlphaBetaModelName + "/parameters/definitions"))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> loadAlphaBetaParameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelParameterDefinition>>() { });
        assertEquals(6, loadAlphaBetaParameterDefinitions.size());

        // --- Get variable definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadPQModelName + "/variables"))
                .andExpect(status().isOk()).andReturn();
        List<ModelVariableDefinition> loadPQVariableDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelVariableDefinition>>() { });
        assertEquals(3, loadPQVariableDefinitions.size());

        // --- Get initial parameter definitions from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + loadPQModelName + "/parameters/definitions"))
                .andExpect(status().isOk()).andReturn();
        List<ModelParameterDefinition> loadPQParameterDefinitions = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<ModelParameterDefinition>>() { });
        assertEquals(4, loadPQParameterDefinitions.size());

        // cross-check variable definitions between two models
        Sets.SetView<ModelVariableDefinition> intersectionVariableDefinitions = Sets.intersection(new HashSet<>(loadAlphaBetaVariableDefinitions), new HashSet<>(loadPQVariableDefinitions));
        assertEquals(3, intersectionVariableDefinitions.size());

        // cross-check parameter definitions between two models
        Sets.SetView<ModelParameterDefinition> intersectionParameterDefinitions = Sets.intersection(new HashSet<>(loadAlphaBetaParameterDefinitions), new HashSet<>(loadPQParameterDefinitions));
        assertEquals(3, intersectionParameterDefinitions.size());

        // the last parameter definition of load alpha beta model must be NETWORK
        ModelParameterDefinition lastParameterDefinitionInLoadAlphaBetaModel = loadAlphaBetaParameterDefinitions.stream().reduce((first, second) -> second).get();
        assertEquals(ParameterOrigin.NETWORK, lastParameterDefinitionInLoadAlphaBetaModel.getOrigin());

        // the last parameter definition of load PQ model must be USER
        ModelParameterDefinition lastParameterDefinitionInLoadPQModel = loadPQParameterDefinitions.stream().reduce((first, second) -> second).get();
        assertEquals(ParameterOrigin.USER, lastParameterDefinitionInLoadPQModel.getOrigin());

        // two last parameter definitions in two models must be the same name, type, originName and fixedValue
        assertEquals(lastParameterDefinitionInLoadAlphaBetaModel.getName(), lastParameterDefinitionInLoadPQModel.getName());
        assertEquals(lastParameterDefinitionInLoadAlphaBetaModel.getType(), lastParameterDefinitionInLoadPQModel.getType());
        assertEquals(lastParameterDefinitionInLoadAlphaBetaModel.getOriginName(), lastParameterDefinitionInLoadPQModel.getOriginName());
        assertEquals(lastParameterDefinitionInLoadAlphaBetaModel.getFixedValue(), lastParameterDefinitionInLoadPQModel.getFixedValue());

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
        ModelEntity savedModel = modelRepository.findById(modelName).orElseThrow();

        // sanity check
        assertEquals(modelName, savedModel.getModelName());
        assertEquals(EquipmentType.GENERATOR, savedModel.getEquipmentType());

        // check variables sets
        assertEquals(2, savedModel.getVariableSets().size());
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
        assertEquals(2, variablesSet.size());
        assertEquals(4, variableDefinitionsOfGeneratorSet.size());
        assertEquals(1, variableDefinitionsOfVoltageRegulatorSet.size());

        // --- Get initial variable sets from GET endpoint --- //
        mvcResult = mvc.perform(get("/models/" + model.getModelName() + "/variables-sets"))
                .andExpect(status().isOk()).andReturn();
        List<VariablesSet> variablesSet1 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<VariablesSet>>() { });
        assertEquals(2, variablesSet1.size());

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
        assertEquals(3, variablesSet2.size());
        assertEquals(4, variableDefinitionsOfGeneratorSet.size());
        assertEquals(1, variableDefinitionsOfVoltageRegulatorSet.size());
        assertEquals(2, variableDefinitionsOfRegulator2Set.size());

        // --- Remove an existing variables set --- //
        mvcResult = mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/remove")
                        .content(objectMapper.writeValueAsString(List.of(variablesSet2.get(2).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets
        Model model3 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet3 = model3.getVariablesSets();

        // Check result
        assertEquals(2, variablesSet3.size());
        // must contains all variables sets after removing
        assertTrue(variablesSet2.containsAll(variablesSet3));

        // --- Remove all variables set --- //
        mvcResult = mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/remove-all")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets
        Model model4 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet4 = model4.getVariablesSets();

        // Check result
        assertEquals(0, variablesSet4.size());

        // --- Add an existing variables set --- //
        mvcResult = mvc.perform(patch("/models/" + model.getModelName() + "/variables-sets/add")
                        .content(objectMapper.writeValueAsString(List.of(variablesSet2.get(2).getName())))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Get variables sets
        Model model5 = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Model.class);
        List<VariablesSet> variablesSet5 = model5.getVariablesSets();

        // Check result
        assertEquals(1, variablesSet5.size());

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
        List<String> deletedVariablesSetNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<String>>() { });

        // Check result
        assertEquals(1, deletedVariablesSetNames.size());
        assertEquals(variablesSet2.get(2).getName(), deletedVariablesSetNames.get(0));

        // -- there is 2 variables set in database, i.e. two initial, one added, one deleted => remain 2 -- //
        mvcResult = mvc.perform(get("/models/variables-sets/names")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<String> foundVariableSetNames = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<String>>() { });
        assertEquals(2, foundVariableSetNames.size());

        mvcResult = mvc.perform(get("/models/variables-sets")
                        .param("variablesSetNames", foundVariableSetNames.stream().collect(Collectors.joining(",")))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<VariablesSet> foundVariableSets = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<VariablesSet>>() { });
        assertEquals(2, foundVariableSets.size());
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

        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + set + "]", true));

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

        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + set + "]", true));

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
        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name + "/" + "FIXED")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[]", true));
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
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findById(loadAlphaBetaModelName);
        assertEquals(false, foundNoneExistingModelOpt.isPresent());

        // must delete only 2 parameter definitions which are only used by load alpha beta
        // the rest 4 shared parameter definitions must be always present in db
        List<ModelParameterDefinitionEntity> foundNoneExistingParameterDefinitions = modelParameterDefinitionRepository.findAllById(List.of("load_alpha", "load_beta"));
        assertEquals(0, foundNoneExistingParameterDefinitions.size());
        List<ModelParameterDefinitionEntity> foundExistingParameterDefinitions = modelParameterDefinitionRepository.findAllById(List.of("load_P0Pu", "load_Q0Pu", "load_U0Pu", "load_UPhase0"));
        assertEquals(4, foundExistingParameterDefinitions.size());

        // must delete only 2 variable definitions which are only used by load alpha beta
        // the rest 3 shared variable definitions must be always present in db
        List<ModelVariableDefinitionEntity> foundNoneExistingVariableDefinitions = modelVariableRepository.findAllById(List.of("load_PRefPu", "load_running_value"));
        assertEquals(0, foundNoneExistingVariableDefinitions.size());
        List<ModelVariableDefinitionEntity> foundExistingVariableDefinitions = modelVariableRepository.findAllById(List.of("load_PPu", "load_QPu", "load_QRefPu"));
        assertEquals(3, foundExistingVariableDefinitions.size());

        // --- Delete LOAD PQ --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(loadPQModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model LOAD PQ must be not exist in db
        foundNoneExistingModelOpt = modelRepository.findById(loadPQModelName);
        assertEquals(false, foundNoneExistingModelOpt.isPresent());

        // db must not contain any parameter definition
        assertEquals(0, modelParameterDefinitionRepository.findAll().size());
        // db must not contain any variable definition
        assertEquals(0, modelVariableRepository.findAll().size());

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
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllById(List.of("Generator", "VoltageRegulator"));
        assertEquals(2, variableSets.size());
        // Variable set Generator contains 4 variable definitions and VoltageRegulator contains 1 variable definition => total = 5
        assertEquals(5, modelVariableRepository.findAll().size());

        // --- Delete model generator --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model generator model must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findById(generatorModelName);
        assertEquals(false, foundNoneExistingModelOpt.isPresent());

        // db must not contain any variable set
        assertEquals(0, modelVariablesSetRepository.findAll().size());
        // db must not contain any variable definition
        assertEquals(0, modelVariableRepository.findAll().size());
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
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllById(List.of("Generator", "VoltageRegulator"));
        assertEquals(2, variableSets.size());
        // Variable set Generator contains 4 variable definitions and VoltageRegulator contains 1 variable definition => total = 5
        assertEquals(5, modelVariableRepository.findAll().size());

        // --- Delete model Three Windings Generator --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorModelThreeWindingsName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Three Windings Generator must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findById(generatorModelThreeWindingsName);
        assertEquals(false, foundNoneExistingModelOpt.isPresent());

        // These models have two shared variable sets => after delete one model, shared shared variable sets must be always in db
        variableSets = modelVariablesSetRepository.findAllById(List.of("Generator", "VoltageRegulator"));
        assertEquals(2, variableSets.size());
        // Variable set Generator contains 4 variable definitions and VoltageRegulator contains 1 variable definition => total = 5
        assertEquals(5, modelVariableRepository.findAll().size());

        // --- Delete model Four Windings Generator --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorModelFourWindingsName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Three Windings Generator must be not exist in db
        foundNoneExistingModelOpt = modelRepository.findById(generatorModelFourWindingsName);
        assertEquals(false, foundNoneExistingModelOpt.isPresent());

        // the last model which uses shared variable sets has been deleted
        // db must not contain any variable set
        assertEquals(0, modelVariablesSetRepository.findAll().size());
        // db must not contain any variable definition
        assertEquals(0, modelVariableRepository.findAll().size());

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
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllById(List.of("GeneratorPQ", "GeneratorPV"));
        assertEquals(2, variableSets.size());
        // Variable set GeneratorPQ and GeneratorPV share 3 variable definitions
        assertEquals(3, modelVariableRepository.findAll().size());

        // --- Delete model Generator PQ --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorPQModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Generator PQ must be not exist in db
        Optional<ModelEntity> foundNoneExistingModelOpt = modelRepository.findById(generatorPQModelName);
        assertEquals(false, foundNoneExistingModelOpt.isPresent());

        // Variable set Generator PQ must be not exist in db
        variableSets = modelVariablesSetRepository.findAllById(List.of("GeneratorPQ"));
        assertEquals(0, variableSets.size());
        // 3 variable definitions used by variable set GeneratorPV must be always present in db
        assertEquals(3, modelVariableRepository.findAll().size());

        // --- Delete model Generator PV --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorPVModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // Variable set Generator PV must be not exist in db
        variableSets = modelVariablesSetRepository.findAllById(List.of("GeneratorPV"));
        assertEquals(0, variableSets.size());
        // 3 variable definitions used by variable set GeneratorPV must be not exist in db
        assertEquals(0, modelVariableRepository.findAll().size());
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
        List<ModelVariableSetEntity> variableSets = modelVariablesSetRepository.findAllById(List.of("GeneratorPQ", "GeneratorPV"));
        assertEquals(2, variableSets.size());
        // Variable set GeneratorPQ and GeneratorPV share 3 variable definitions
        assertEquals(3, modelVariableRepository.findAll().size());

        // --- Delete model Generator PQ --- //
        mvc.perform(delete("/models/")
                        .content(objectMapper.writeValueAsString(List.of(generatorPQModelName, generatorPVModelName)))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        // --- Check result --- //
        // model Generator PQ/PV must be not exist in db
        List<ModelEntity> modelEntities = modelRepository.findAllById(List.of(generatorPQModelName, generatorPVModelName));
        assertEquals(0, modelEntities.size());

        // Variable set Generator PQ/PV must be not exist in db
        variableSets = modelVariablesSetRepository.findAllById(List.of("GeneratorPV", "GeneratorPQ"));
        assertEquals(0, variableSets.size());
        // 3 variable definitions used by variable sets GeneratorPQ/PV must be not exist in db
        assertEquals(0, modelVariableRepository.findAll().size());
    }
}
