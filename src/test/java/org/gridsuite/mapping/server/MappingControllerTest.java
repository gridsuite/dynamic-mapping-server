/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

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
public class MappingControllerTest {

    public static final String RESOURCE_PATH_DELIMETER = "/";
    public static final String TEST_DATA_DIR = RESOURCE_PATH_DELIMETER + "data";

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingControllerTest.class);

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void cleanDB() {
        mappingRepository.deleteAll();
        modelRepository.deleteAll();
    }

    @Before
    public void setUp() {
        cleanDB();
    }

    String mapping(String name) {
        return """
                {
                    "name":"%s",
                    "rules":[
                        {
                            "composition":"filter1 && filter2 && filter3 && filter4",
                            "equipmentType":"GENERATOR",
                            "filters":[
                                {
                                    "filterId":"filter1",
                                    "operand":"EQUALS",
                                    "property":"id",
                                    "value":[
                                        "test"
                                    ],
                                    "type":"STRING"
                                },
                                {
                                    "filterId":"filter2",
                                    "operand":"HIGHER",
                                    "property":"minP",
                                    "value":[
                                        3.0
                                    ],
                                    "type":"NUMBER"
                                },
                                {
                                    "filterId":"filter3",
                                    "operand":"IN",
                                    "property":"energySource",
                                    "value":[
                                        "OTHERS"
                                    ],
                                    "type":"ENUM"
                                },
                                {
                                    "filterId":"filter4",
                                    "operand":"NOT_EQUALS",
                                    "property":"voltageRegulatorOn",
                                    "value":true,
                                    "type":"BOOLEAN"
                                }
                            ],
                            "mappedModel":"mappedExample",
                            "setGroup":"setGroup",
                            "groupType":"FIXED"
                        }
                    ],
                    "automata":[
                        {
                            "family":"CURRENT_LIMIT",
                            "model":"OverloadManagementSystem",
                            "setGroup":"automaton_group",
                            "properties":[
                                {
                                    "name":"dynamicModelId",
                                    "value":"cla_automaton_name",
                                    "type":"STRING"
                                },
                                {
                                    "name":"iMeasurement",
                                    "value":"element_id",
                                    "type":"STRING"
                                },
                                {
                                    "name":"iMeasurementSide",
                                    "value":"TwoSides.ONE",
                                    "type":"ENUM"
                                },
                                {
                                    "name":"controlledBranch",
                                    "value":"element_id",
                                    "type":"ENUM"
                                }
                            ]
                        },
                        {
                            "family":"VOLTAGE",
                            "model":"TapChangerBlockingAutomationSystem",
                            "setGroup":"automaton_group_2",
                            "properties":[
                                {
                                    "name":"dynamicModelId",
                                    "value":"tcb_automaton_name",
                                    "type":"STRING"
                                },
                                {
                                    "name":"uMeasurement",
                                    "value":"bus_id_1, bus_id_2",
                                    "type":"STRING"
                                },
                                {
                                    "name":"transformers",
                                    "value":"load_id_1, load_id_2",
                                    "type":"STRING"
                                }
                            ]
                        }
                    ],
                    "controlledParameters":false
                }
            """
            .formatted(name);

    }

    @Test
    public void test() throws Exception {

        String name = "test";

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(mapping(name))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get all data
        mvc.perform(get("/mappings/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + mapping(name) + "]", true));

        // delete data
        mvc.perform(delete("/mappings/" + name))
                .andExpect(status().isOk());

        // get to verify deletion
        mvc.perform(get("/mappings/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    public void testRename() throws Exception {

        String originalName = "origin";

        String newName = "new";

        // Put data
        mvc.perform(post("/mappings/" + originalName)
                        .content(mapping(originalName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Rename data
        mvc.perform(post("/mappings/rename/" + originalName + "/to/" + newName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get all data
        mvc.perform(get("/mappings/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + mapping(newName) + "]", true));

        // Add a new mapping
        mvc.perform(post("/mappings/" + originalName)
                        .content(mapping(originalName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Fail to rename to existing mapping
        mvc.perform(post("/mappings/rename/" + originalName + "/to/" + newName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());

        // Fail to rename from missing mapping
        mvc.perform(post("/mappings/rename/NotUsed/to/AnyMapping"
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testCopy() throws Exception {

        String originalName = "origin";

        String copyName = "copy";

        // Put data
        mvc.perform(post("/mappings/" + originalName)
                        .content(mapping(originalName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Rename data
        mvc.perform(post("/mappings/copy/" + originalName + "/to/" + copyName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get all data
        mvc.perform(get("/mappings/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                // Content served in alphabetical order since name  is the id
                .andExpect(content().json("[" + mapping(originalName) + ", " + mapping(copyName) + "]", true));

        // Add a new mapping
        mvc.perform(post("/mappings/" + originalName)
                        .content(mapping(originalName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Fail to copy to existing mapping
        mvc.perform(post("/mappings/copy/" + originalName + "/to/" + copyName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());

        // Fail to copy from missing mapping
        mvc.perform(post("/mappings/copy/NotUsed/to/AnyMapping"
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @Transactional
    public void testGetMappedModelsList() throws Exception {
        // put LoadAlphaBetaModel model
        InputStream isLoadAlphaBetaModel = getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMETER + "model/load/loadAlphaBeta.json");
        String alphaBetaModelJson = new String(isLoadAlphaBetaModel.readAllBytes());
        mvc.perform(post("/models/")
                        .content(alphaBetaModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // put GeneratorSynchronousThreeWindingsProportionalRegulations model
        InputStream isGeneratorSynchronousThreeWindingsProportionalRegulations =
                getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMETER + "model/generator/generatorSynchronousThreeWindingsProportionalRegulations.json");
        String generatorSynchronousThreeWindingsProportionalRegulationsJson = new String(isGeneratorSynchronousThreeWindingsProportionalRegulations.readAllBytes());
        mvc.perform(post("/models/")
                        .content(generatorSynchronousThreeWindingsProportionalRegulationsJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // put StaticVarCompensator model
        InputStream isStaticVarCompensator =
                getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMETER + "model/svarc/staticVarCompensator.json");
        String staticVarCompensatorJson = new String(isStaticVarCompensator.readAllBytes());
        mvc.perform(post("/models/")
                        .content(staticVarCompensatorJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // put a mapping which uses the saved models
        String mappingJson = new String(getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMETER + "mapping/mapping_01.json").readAllBytes());
        // Put data
        mvc.perform(post("/mappings/" + "mapping_01")
                        .content(mappingJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // main test : get the list of used models in the mapping
        MvcResult result = mvc.perform(get("/mappings/" + "mapping_01" + "/models"))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        String resultMappedModelsListJson = result.getResponse().getContentAsString();
        LOGGER.info("resultMappedModelsListJson : " + resultMappedModelsListJson);
        List<Model> resultMappedModelsList = objectMapper.readValue(resultMappedModelsListJson, new TypeReference<List<Model>>() { });
        // must contain at least LoadAlphaBeta model
        assertTrue(resultMappedModelsList.stream().anyMatch(model -> Objects.equals("LoadAlphaBeta", model.getModelName())));
        assertTrue(resultMappedModelsList.stream().anyMatch(model -> Objects.equals("GeneratorSynchronousThreeWindingsProportionalRegulations", model.getModelName())));
        assertTrue(resultMappedModelsList.stream().anyMatch(model -> Objects.equals("StaticVarCompensator", model.getModelName())));

    }
}
