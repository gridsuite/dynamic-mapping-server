/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
public class MappingControllerTest {

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private MockMvc mvc;

    private void cleanDB() {
        mappingRepository.deleteAll();
        scriptRepository.deleteAll();
    }

    @Before
    public void setUp() {
        cleanDB();
    }

    @Test
    public void test() throws Exception {
        String script =
                "{\n" +
                        "  \"name\": \"test\",\n" +
                        "  \"rules\": [\n" +
                        "    {\n" +
                        "      \"composition\": \"filter1\",\n" +
                        "      \"equipmentType\": \"GENERATOR\",\n" +
                        "      \"filters\": [\n" +
                        "        {\n" +
                        "          \"filterId\": \"filter1\",\n" +
                        "          \"operand\": \"EQUALS\",\n" +
                        "          \"property\": \"id\",\n" +
                        "          \"value\": \"test\",\n" +
                        "          \"type\": \"STRING\"\n" +
                        "        }\n" +
                        "      ],\n" +
                        "      \"mappedModel\": \"mappedExample\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

        // Put data
        mvc.perform(post("/mappings/test")
                .content(script)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get all data
        mvc.perform(get("/mappings/")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + script + "]", true));

        // delete data
        mvc.perform(delete("/mappings/test"))
                .andExpect(status().isOk());

        // get to verify deletion
        mvc.perform(get("/mappings/")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }
}
