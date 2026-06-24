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
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.models.Model;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.service.client.filter.FilterClient;
import org.gridsuite.mapping.server.utils.FilterClientMockUtils;
import org.gridsuite.mapping.server.utils.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
public class MappingControllerTest {
    public static final String RESOURCE_PATH_DELIMITER = "/";
    public static final String TEST_DATA_DIR = RESOURCE_PATH_DELIMITER + "data";
    public static final String MAPPING_FILE = "mapping_01.json";
    public static final String MAPPING_FILE_NAME = "mapping_01";

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingControllerTest.class);

    private final Map<UUID, ExpertFilter> filtersMockDB = new HashMap<>();

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    ModelRepository modelRepository;

    @MockitoBean
    FilterClient filterClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier(RestConfig.EXPORT_MAPPING_OBJECT_MAPPER_BEAN)
    ObjectMapper exportMappingObjectMapper;

    private void cleanDB() {
        mappingRepository.deleteAll();
        modelRepository.deleteAll();

        filtersMockDB.clear();
    }

    @Before
    public void setUp() {
        FilterClientMockUtils.mockAll(filtersMockDB, filterClient, objectMapper);
        cleanDB();
    }

    @Test
    public void test() throws Exception {
        String mappingPath = TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "mapping" + RESOURCE_PATH_DELIMITER + MAPPING_FILE;
        InputMapping inputMapping = objectMapper.readValue(getClass().getResourceAsStream(mappingPath), InputMapping.class);

        // Create data
        MvcResult mvcResult = mvc.perform(post("/mappings")
                        .content(objectMapper.writeValueAsString(inputMapping))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UUID mappingId = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UUID.class);

        // Get one mapping
        mvcResult = mvc.perform(get("/mappings/" + mappingId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();
        InputMapping mapping = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), InputMapping.class);
        assertThat(mapping.getId()).isEqualTo(mappingId);
        Assertions.assertThat(mapping).recursivelyEquals(inputMapping);

        // get all data
        mvcResult = mvc.perform(get("/mappings")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();
        List<InputMapping> mappings = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(mappings.get(0).getId()).isEqualTo(mappingId);
        Assertions.assertThat(mappings.get(0)).recursivelyEquals(inputMapping);

        // Update a mapping
        InputMapping mappingToUpdate = mappings.getFirst();
        int ruleSizeOrigin = mappingToUpdate.getRules().size();
        mappingToUpdate.getRules().removeFirst(); // remove a rule to test update
        mvc.perform(put("/mappings/" + mappingId)
                        .content(objectMapper.writeValueAsString(mappingToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
        // check updated mapping => check that the rule has been removed
        mvcResult = mvc.perform(get("/mappings/" + mappingId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();
        InputMapping updatedMapping = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), InputMapping.class);
        assertThat(updatedMapping.getId()).isEqualTo(mappingId);
        assertThat(updatedMapping.getRules()).hasSize(ruleSizeOrigin - 1);

        // does not perform update if not exist
        mvc.perform(put("/mappings/" + UUID.randomUUID())
                        .content(objectMapper.writeValueAsString(inputMapping))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // delete data
        mvc.perform(delete("/mappings/" + mappingId))
                .andExpect(status().isOk());

        // get to verify deletion
        mvc.perform(get("/mappings")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    public void testCopy() throws Exception {

        String mappingPath = TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "mapping" + RESOURCE_PATH_DELIMITER + MAPPING_FILE;
        InputMapping inputMapping = objectMapper.readValue(getClass().getResourceAsStream(mappingPath), InputMapping.class);

        // Create data
        MvcResult mvcResult = mvc.perform(post("/mappings")
                        .content(objectMapper.writeValueAsString(inputMapping))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UUID originId = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UUID.class);

        // copy data
        mvcResult = mvc.perform(post("/mappings/" + originId + "/duplicate")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UUID copyId = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UUID.class);

        // get all data
        mvcResult = mvc.perform(get("/mappings")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();
        List<InputMapping> mappings = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertThat(mappings.get(0).getId()).isEqualTo(originId);
        assertThat(mappings.get(1).getId()).isEqualTo(copyId);
        Assertions.assertThat(mappings.get(0)).recursivelyEquals(inputMapping);
        Assertions.assertThat(mappings.get(1)).recursivelyEquals(inputMapping);

    }

    @Test
    @Transactional
    public void testGetMappedModelsList() throws Exception {
        // put LoadAlphaBetaModel model
        InputStream isLoadAlphaBetaModel = getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "model/load/loadAlphaBeta.json");
        String alphaBetaModelJson = new String(isLoadAlphaBetaModel.readAllBytes());
        mvc.perform(post("/models/")
                        .content(alphaBetaModelJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // put GeneratorSynchronousThreeWindingsProportionalRegulations model
        InputStream isGeneratorSynchronousThreeWindingsProportionalRegulations =
                getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "model/generator/generatorSynchronousThreeWindingsProportionalRegulations.json");
        String generatorSynchronousThreeWindingsProportionalRegulationsJson = new String(isGeneratorSynchronousThreeWindingsProportionalRegulations.readAllBytes());
        mvc.perform(post("/models/")
                        .content(generatorSynchronousThreeWindingsProportionalRegulationsJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // put StaticVarCompensator model
        InputStream isStaticVarCompensator =
                getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "model/svarc/staticVarCompensator.json");
        String staticVarCompensatorJson = new String(isStaticVarCompensator.readAllBytes());
        mvc.perform(post("/models/")
                        .content(staticVarCompensatorJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // put a mapping which uses the saved models
        String mappingJson = new String(getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "mapping/mapping_01.json").readAllBytes());
        // Put data
        MvcResult mvcResult = mvc.perform(post("/mappings")
                        .content(mappingJson)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UUID mappingId = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UUID.class);

        // main test : get the list of used models in the mapping
        mvcResult = mvc.perform(get("/mappings/" + mappingId + "/models"))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        String resultMappedModelsListJson = mvcResult.getResponse().getContentAsString();
        LOGGER.info("resultMappedModelsListJson : " + resultMappedModelsListJson);
        List<Model> resultMappedModelsList = objectMapper.readValue(resultMappedModelsListJson, new TypeReference<>() { });
        // must contain at least LoadAlphaBeta model
        assertThat(resultMappedModelsList.stream().anyMatch(model -> Objects.equals("LoadAlphaBeta", model.getModelName()))).isTrue();
        assertThat(resultMappedModelsList.stream().anyMatch(model -> Objects.equals("GeneratorSynchronousThreeWindingsProportionalRegulations", model.getModelName()))).isTrue();
        assertThat(resultMappedModelsList.stream().anyMatch(model -> Objects.equals("StaticVarCompensator", model.getModelName()))).isTrue();
    }

    @Test
    public void testExportMapping() throws Exception {
        // --- Load the input mapping file ---
        String mappingPath = TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "mapping" + RESOURCE_PATH_DELIMITER + MAPPING_FILE;
        InputMapping inputMapping = objectMapper.readValue(getClass().getResourceAsStream(mappingPath), InputMapping.class);
        String mappingJson = objectMapper.writeValueAsString(inputMapping);
        // --- Save the mapping via POST /mappings/{name} ---
        MvcResult mvcResult = mvc.perform(post("/mappings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mappingJson))
                .andExpect(status().isOk())
                .andReturn();

        UUID mappingId = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UUID.class);

        // --- Export the mapping via GET /mappings/{mappingId}/export ---
        MvcResult exportResult = mvc.perform(
                        get("/mappings/{mappingId}/export", mappingId)
                                .param("fileName", MAPPING_FILE_NAME))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "form-data; name=\"attachment\"; filename=\"" + MAPPING_FILE_NAME + ".json\""))
                .andReturn();

        byte[] exportedBytes = exportResult.getResponse().getContentAsByteArray();

        // --- Build expected JSON using the injected export mapper bean (same as production) ---
        inputMapping = objectMapper.readValue(mappingJson, InputMapping.class);
        byte[] expectedBytes = exportMappingObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(inputMapping);

        // --- Compare as JSON trees (order- and whitespace-insensitive) ---
        JsonNode exportedTree = objectMapper.readTree(exportedBytes);
        JsonNode expectedTree = objectMapper.readTree(expectedBytes);

        assertEquals(expectedTree, exportedTree,
                "Exported mapping JSON does not match the expected content");
    }
}
