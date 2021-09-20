/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
public class ModelControllerTest {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private MockMvc mvc;

    private void cleanDB() {
        modelRepository.deleteAll();
    }

    private ModelParameterDefinitionEntity createDefinitionEntity(String name, ParameterType type, ParameterOrigin origin, String originName, ModelEntity model) {
        return new ModelParameterDefinitionEntity(name, model.getModelName(), type, origin, originName, null, model);
    }

    @Before
    public void setUp() {
        cleanDB();

        // prepare token model
        ModelEntity modelToSave = new ModelEntity("LoadAlphaBeta", EquipmentType.LOAD,
                null, new ArrayList<>());
        ArrayList<ModelParameterDefinitionEntity> definitions = new ArrayList<>();
        definitions.add(createDefinitionEntity("load_alpha", ParameterType.DOUBLE, ParameterOrigin.USER, null, modelToSave));
        definitions.add(createDefinitionEntity("load_beta", ParameterType.DOUBLE, ParameterOrigin.USER, null, modelToSave));
        definitions.add(createDefinitionEntity("load_P0Pu", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "p_pu", modelToSave));
        definitions.add(createDefinitionEntity("load_Q0Pu", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "q_pu", modelToSave));
        definitions.add(createDefinitionEntity("load_U0Pu", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "v_pu", modelToSave));
        definitions.add(createDefinitionEntity("load_UPhase0", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "angle_pu", modelToSave));
        modelToSave.setParameterDefinitions(definitions);
        modelRepository.save(modelToSave);
    }

    @Test
    @Transactional
    public void test() throws Exception {

        String name = "setName";
        String modelName = "LoadAlphaBeta";
        String set = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"name\": \"load_alpha\",\n" +
                "      \"value\": \"1.5\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"load_beta\",\n" +
                "      \"value\": \"2.5\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String setGroup = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"modelName\": \"" + modelName + "\",\n" +
                "  \"type\": \"FIXED\",\n" +
                "  \"sets\": [\n" +
                set +
                "  ]\n" +
                "}";

        // Put data
        mvc.perform(post("/models/" + modelName + "/parameters/sets/strict")
                        .content(setGroup)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/models/" + modelName + "/parameters/sets/" + name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + set + "]", true));

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

        mvc.perform(get("/models/" + modelName + "/parameters/definitions/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[\n" +
                        "{\"name\":\"load_alpha\",\n \"type\":\"DOUBLE\",\"origin\":\"USER\",\"originName\":null,\"fixedValue\":null},\n" +
                        "{\"name\":\"load_beta\",\"type\":\"DOUBLE\",\"origin\":\"USER\",\"originName\":null,\"fixedValue\":null},\n" +
                        "{\"name\":\"load_P0Pu\",\"type\":\"DOUBLE\",\"origin\":\"NETWORK\",\"originName\":\"p_pu\",\"fixedValue\":null},\n" +
                        "{\"name\":\"load_Q0Pu\",\"type\":\"DOUBLE\",\"origin\":\"NETWORK\",\"originName\":\"q_pu\",\"fixedValue\":null},\n" +
                        "{\"name\":\"load_U0Pu\",\"type\":\"DOUBLE\",\"origin\":\"NETWORK\",\"originName\":\"v_pu\",\"fixedValue\":null},\n" +
                        "{\"name\":\"load_UPhase0\",\"type\":\"DOUBLE\",\"origin\":\"NETWORK\",\"originName\":\"angle_pu\",\"fixedValue\":null}\n" +
                        "]", true));
    }

    @Test
    public void invalidTest() throws Exception {

        String name = "errorSet";
        String modelName = "LoadAlphaBeta";
        String set = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"modelName\": \"" + modelName + "\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"name\": \"load_alpha\",\n" +
                "      \"value\": \"1.5\"\n" +
                "    },\n" +
                "  ]\n" +
                "}";
        // Put data
        String setGroup = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"modelName\": \"" + modelName + "\",\n" +
                "  \"type\": \"FIXED\",\n" +
                "  \"sets\": [\n" +
                set +
                "  ]\n" +
                "}";

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
        ModelParameterSetEntity setToSave = new ModelParameterSetEntity("LAB", loadGroup.getName(), loadModel.getModelName(),
                null,
                new Date(),
                loadGroup);
        ArrayList<ModelParameterEntity> setParameters = new ArrayList<>();
        setParameters.add(new ModelParameterEntity("load_alpha", loadGroup.getModelName(), loadGroup.getName(), setToSave.getName(), "1.5", setToSave));
        setParameters.add(new ModelParameterEntity("load_beta", loadGroup.getModelName(), loadGroup.getName(), setToSave.getName(), "2.5", setToSave));
        setToSave.setParameters(setParameters);
        groupSets.add(setToSave);
        loadGroup.setSets(groupSets);
        loadGroups.add(loadGroup);
        loadModel.setSetsGroups(loadGroups);
        modelRepository.save(loadModel);

        ModelEntity generatorThreeModel = new ModelEntity("GeneratorThreeWindings", EquipmentType.GENERATOR, new ArrayList<>(), null);
        ArrayList<ModelSetsGroupEntity> generatorThreeGroups = new ArrayList<>();
        generatorThreeGroups.add(new ModelSetsGroupEntity("GSTWPR", generatorThreeModel.getModelName(), new ArrayList<>(), SetGroupType.PREFIX, generatorThreeModel));
        generatorThreeModel.setSetsGroups(generatorThreeGroups);
        modelRepository.save(generatorThreeModel);

        mvc.perform(get("/models/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[\n" +
                        "{\"name\":\"LoadAlphaBeta\",\n \"type\":\"LOAD\",\"groups\":[{\"name\": \"LAB\", \"type\": \"FIXED\"}]},\n" +
                        "{\"name\":\"GeneratorThreeWindings\",\n \"type\":\"GENERATOR\",\"groups\":[{\"name\": \"GSTWPR\", \"type\": \"PREFIX\"}]}\n" +
                        "]", true));
    }
}
