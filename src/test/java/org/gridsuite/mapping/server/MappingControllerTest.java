/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.repository.MappingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private MockMvc mvc;

    private void cleanDB() {
        mappingRepository.deleteAll();
    }

    @Before
    public void setUp() {
        cleanDB();
    }

    String mapping(String name) {
        return "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"composition\": \"filter1 && filter2 && filter3 && filter4\",\n" +
                "      \"equipmentType\": \"GENERATOR\",\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"filterId\": \"filter1\",\n" +
                "          \"operand\": \"EQUALS\",\n" +
                "          \"property\": \"id\",\n" +
                "          \"value\": [\"test\"],\n" +
                "          \"type\": \"STRING\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterId\": \"filter2\",\n" +
                "          \"operand\": \"HIGHER\",\n" +
                "          \"property\": \"minP\",\n" +
                "          \"value\": [3.0],\n" +
                "          \"type\": \"NUMBER\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterId\": \"filter3\",\n" +
                "          \"operand\": \"IN\",\n" +
                "          \"property\": \"energySource\",\n" +
                "          \"value\": [\"OTHERS\"],\n" +
                "          \"type\": \"STRING\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterId\": \"filter4\",\n" +
                "          \"operand\": \"NOT_EQUALS\",\n" +
                "          \"property\": \"voltageRegulatorOn\",\n" +
                "          \"value\": true,\n" +
                "          \"type\": \"BOOLEAN\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"mappedModel\": \"mappedExample\",\n" +
                "      \"setGroup\": \"setGroup\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"automata\": [\n" +
                "    {\n" +
                "      \"family\": \"CURRENT_LIMIT\",\n" +
                "      \"model\": \"automaton_model\",\n" +
                "      \"setGroup\": \"automaton_group\",\n" +
                "      \"watchedElement\": \"element_id\",\n" +
                "      \"side\": \"Branch.Side.ONE\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"controlledParameters\": false" +
                "}";

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
}
