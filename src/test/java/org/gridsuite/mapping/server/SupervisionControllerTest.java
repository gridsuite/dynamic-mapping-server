/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.repository.RuleRepository;
import org.gridsuite.mapping.server.utils.EquipmentType;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mathieu Deharbe <mathieu.deharbe at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
public class SupervisionControllerTest {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID filter1Uuid = UUID.randomUUID();
    private final UUID filter2Uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        cleanDB();
        RuleEntity rule1 = new RuleEntity(UUID.randomUUID(), EquipmentType.LINE, "null", "setGroup", SetGroupType.FIXED, filter1Uuid, null);
        RuleEntity rule2 = new RuleEntity(UUID.randomUUID(), EquipmentType.LINE, "null", "setGroup", SetGroupType.FIXED, filter2Uuid, null);
        ruleRepository.save(rule1);
        ruleRepository.save(rule2);
    }

    private void cleanDB() {
        ruleRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testGetFiltersList() throws Exception {
        MvcResult result = mvc.perform(get("/supervision/filters"))
                .andExpect(status().isOk())
                .andReturn();

        // check result
        String filtersJson = result.getResponse().getContentAsString();
        List<UUID> filters = objectMapper.readValue(filtersJson, new TypeReference<>() { });
        assertThat(filters).containsExactlyInAnyOrder(filter1Uuid, filter2Uuid);
    }
}
