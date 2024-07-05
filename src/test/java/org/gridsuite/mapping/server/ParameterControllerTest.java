/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.ModelParameterDefinitionRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.service.client.filter.FilterClient;
import org.gridsuite.mapping.server.utils.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
public class ParameterControllerTest {
    public static final String RESOURCE_PATH_DELIMITER = "/";
    public static final String TEST_DATA_DIR = RESOURCE_PATH_DELIMITER + "data";
    public static final String MAPPING_FILE = "mapping_01.json";
    private final Map<UUID, ExpertFilter> filtersMockDB = new HashMap<>();

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelParameterDefinitionRepository modelParameterDefinitionRepository;

    @MockBean
    FilterClient filterClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void cleanDB() {
        modelRepository.deleteAll();
        modelParameterDefinitionRepository.deleteAll();
    }

    private ModelParameterDefinitionEntity createDefinitionEntity(String name, ParameterType type) {
        return new ModelParameterDefinitionEntity(new ModelParameterDefinition(name, type, null, null, null));
    }

    @Before
    public void setUp() {
        FilterClientMockUtils.mockAll(filtersMockDB, filterClient, objectMapper);
        cleanDB();

        // Prepare models
        ModelEntity loadModel = new ModelEntity("LoadAlphaBeta", EquipmentType.LOAD, false, new ArrayList<>(), null, Set.of(), Set.of(), null, null);
        ArrayList<ModelSetsGroupEntity> loadGroups = new ArrayList<>();
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

        List<ModelParameterDefinitionEntity> definitions = new ArrayList<>();

        // add "USER" parameter definitions
        definitions.add(createDefinitionEntity("load_alpha", ParameterType.DOUBLE));
        definitions.add(createDefinitionEntity("load_beta", ParameterType.DOUBLE));
        loadModel.addAllParameterDefinition(definitions, ParameterOrigin.USER, null);

        definitions.clear();

        // add "NETWORK" parameter definitions
        loadModel.addParameterDefinition(createDefinitionEntity("load_P0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "p_pu");
        loadModel.addParameterDefinition(createDefinitionEntity("load_Q0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "q_pu");
        loadModel.addParameterDefinition(createDefinitionEntity("load_U0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "v_pu");
        loadModel.addParameterDefinition(createDefinitionEntity("load_UPhase0", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "angle_pu");

        modelRepository.save(loadModel);

        ModelEntity generatorThreeModel = new ModelEntity("GeneratorThreeWindings", EquipmentType.GENERATOR, false, null, null, null, null, null, null);
        ArrayList<ModelSetsGroupEntity> generatorThreeGroups = new ArrayList<>();
        generatorThreeGroups.add(new ModelSetsGroupEntity("GSTWPR", generatorThreeModel.getModelName(), null, SetGroupType.PREFIX, generatorThreeModel));
        generatorThreeModel.setSetsGroups(generatorThreeGroups);
        modelRepository.save(generatorThreeModel);

        ModelEntity generatorFourModel = new ModelEntity("GeneratorFourWindings", EquipmentType.GENERATOR, false, null, null, null, null, null, null);
        ArrayList<ModelSetsGroupEntity> generatorFourGroups = new ArrayList<>();
        generatorFourGroups.add(new ModelSetsGroupEntity("GSFWPR", generatorFourModel.getModelName(), null, SetGroupType.PREFIX, generatorFourModel));
        generatorFourModel.setSetsGroups(generatorFourGroups);
        modelRepository.save(generatorFourModel);

        ModelEntity sVarModel = new ModelEntity("StaticVarCompensator", EquipmentType.STATIC_VAR_COMPENSATOR, false, null, null, null, null, null, null);
        ArrayList<ModelSetsGroupEntity> sVarModelGroups = new ArrayList<>();
        sVarModelGroups.add(new ModelSetsGroupEntity("SVarC", sVarModel.getModelName(), null, SetGroupType.PREFIX, sVarModel));
        sVarModel.setSetsGroups(sVarModelGroups);
        modelRepository.save(sVarModel);
    }

    private String baseParameterJson(String parentName, String parametersFile) {
        return """
                {
                "parentName": "%s",
                "parametersFile": %s
                }
                """.formatted(parentName, parametersFile);
    }

    @Test
    @Transactional
    public void getParametersTest() throws Exception {

        String name = "test";

        String mappingPath = TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "mapping" + RESOURCE_PATH_DELIMITER + MAPPING_FILE;
        InputMapping inputMapping = objectMapper.readValue(getClass().getResourceAsStream(mappingPath), InputMapping.class);

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(objectMapper.writeValueAsString(inputMapping))
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get parameter
        mvc.perform(get("/parameters")
                .queryParam("mappingName", name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(baseParameterJson(name, null), true));

        // try to get parameter with unknown mapping
        mvc.perform(get("/parameters")
                        .queryParam("mappingName", "unknown")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Parameters tests
    @Test
    public void parTest() throws Exception {
        String name = "test";
        String modelName = "LoadAlphaBeta";
        String groupName = "LAB";
        String mappingToTest = """
            {
                "name":"%s",
                "rules":[
                    {
                        "equipmentType":"LOAD",
                        "mappedModel":"%s",
                        "setGroup":"%s",
                        "groupType":"FIXED"
                    }
                ],
                "automata":[],
                "controlledParameters":true
            }
            """
            .formatted(name, modelName, groupName);

        String parFile = """
            <?xml version="1.0" encoding="UTF-8"?>
            <parametersSet xmlns="http://www.rte-france.com/dynawo">
                <set id="LAB">
                    <par type="DOUBLE" name="load_alpha" value="1.5"/>
                    <par type="DOUBLE" name="load_beta" value="2.5"/>
                    <reference type="DOUBLE" name="load_P0Pu" origData="IIDM" origName="p_pu"/>
                    <reference type="DOUBLE" name="load_Q0Pu" origData="IIDM" origName="q_pu"/>
                    <reference type="DOUBLE" name="load_U0Pu" origData="IIDM" origName="v_pu"/>
                    <reference type="DOUBLE" name="load_UPhase0" origData="IIDM" origName="angle_pu"/>
                </set>
            </parametersSet>""";

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(mappingToTest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/parameters")
                        .queryParam("mappingName", name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(baseParameterJson(name, objectMapper.writeValueAsString(parFile)), true));
    }
}
