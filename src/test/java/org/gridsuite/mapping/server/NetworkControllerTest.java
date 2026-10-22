/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.mapping.server.dto.NetworkValues;
import org.gridsuite.mapping.server.dto.RuleToMatch;
import org.gridsuite.mapping.server.service.NetworkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
class NetworkControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkControllerTest.class);

    static final String RESOURCE_PATH_DELIMITER = "/";
    static final String TEST_DATA_DIR = RESOURCE_PATH_DELIMITER + "data";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockitoSpyBean
    NetworkService networkService;

    @MockitoBean
    private NetworkStoreService networkStoreService;

    @Test
    void idTest() throws Exception {
        UUID networkUUID = UUID.randomUUID();

        Network testNetwork = NetworkTest1Factory.create();
        when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenReturn(testNetwork);

        MvcResult mvcResult = mvc.perform(get("/network/" + networkUUID + "/values")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String resultNetworkValuesJson = mvcResult.getResponse().getContentAsString();
        NetworkValues resultNetworkValues = objectMapper.readValue(resultNetworkValuesJson, NetworkValues.class);
        resultNetworkValuesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNetworkValues);

        String networkValuesJson = new String(getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "network/networkValues.json").readAllBytes());
        NetworkValues networkValues = objectMapper.readValue(networkValuesJson, NetworkValues.class);

        String expectNetworkValuesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(networkValues);
        LOGGER.info("expect network values = " + expectNetworkValuesJson);
        LOGGER.info("result network values = " + resultNetworkValuesJson);

        assertEquals(objectMapper.readTree(expectNetworkValuesJson), objectMapper.readTree(resultNetworkValuesJson));

        verify(networkService, times(1)).getNetworkValuesFromExistingNetwork(networkUUID);
    }

    @Test
    void unknownNetworkTest() throws Exception {
        UUID networkUUID = UUID.randomUUID();

        when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenThrow(new PowsyblException());

        mvc.perform(MockMvcRequestBuilders.get("/network/" + networkUUID + "/values")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(networkStoreService, times(1)).getNetwork(networkUUID, PreloadingStrategy.COLLECTION);

    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForStaticVarCompensatorTest"
    })
    void ruleMatchingTest(Network testNetwork, String ruleToMatchFile, int ruleIndex, List<String> expectedMatchedIds) throws Exception {
        UUID networkUUID = UUID.randomUUID();

        when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenReturn(testNetwork);

        String ruleToMatchPath = TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "network" + RESOURCE_PATH_DELIMITER + ruleToMatchFile;
        RuleToMatch ruleToMatch = objectMapper.readValue(getClass().getResourceAsStream(ruleToMatchPath), RuleToMatch.class);
        ruleToMatch.setRuleIndex(ruleIndex);
        mvc.perform(MockMvcRequestBuilders.post("/network/" + networkUUID + "/matches/rule")
                        .content(objectMapper.writeValueAsString(ruleToMatch))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            {
                                "ruleIndex": %d,
                                "matchedIds":[%s]
                            }
                        """
                        .formatted(ruleIndex, expectedMatchedIds.stream().collect(Collectors.joining(", "))), true));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {
        Network testNetwork = NetworkTest1Factory.create();
        return Stream.of(
            Arguments.of(testNetwork, "generatorRuleToMatch.json", 1, List.of("generator1"))
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {
        Network testNetwork = NetworkTest1Factory.create();
        return Stream.of(
            Arguments.of(testNetwork, "loadRuleToMatch.json", 10, List.of("load1"))
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {
        Network testNetwork = SvcTestCaseFactory.create();
        return Stream.of(
            Arguments.of(testNetwork, "svarRuleToMatch.json", 20, List.of("SVC2"))
        );
    }

}
