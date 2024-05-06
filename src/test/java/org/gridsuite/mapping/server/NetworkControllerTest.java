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
import org.gridsuite.mapping.server.model.NetworkEntity;
import org.gridsuite.mapping.server.repository.NetworkRepository;
import org.gridsuite.mapping.server.service.NetworkService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.mapping.server.MappingConstants.CASE_API_VERSION;
import static org.gridsuite.mapping.server.MappingConstants.NETWORK_CONVERSION_API_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
class NetworkControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkControllerTest.class);

    public static final String RESOURCE_PATH_DELIMITER = "/";
    public static final String TEST_DATA_DIR = RESOURCE_PATH_DELIMITER + "data";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @SpyBean
    NetworkService networkService;

    @MockBean
    private NetworkStoreService networkStoreService;

    @BeforeEach
    public void setUp() {
        networkRepository.deleteAll();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    String caseApiUri = "http://localhost:5000/";
    String networkConversionApiUri = "http://localhost:5003/";

    @Test
    public void fileTest() throws Exception {

        UUID caseUUID = UUID.randomUUID();
        UUID networkUUID = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.iidm",
                MediaType.TEXT_PLAIN_VALUE,
                "This is a network".getBytes());

        // Mock call to case-server for import
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(caseApiUri + CASE_API_VERSION + "/cases")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(TEXT_PLAIN)
                        .body("\"" + caseUUID + "\""));

        // Mock call to case-server for conversion
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(networkConversionApiUri + NETWORK_CONVERSION_API_VERSION + "/networks?caseUuid=" + caseUUID + "&isAsyncRun=false")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(APPLICATION_JSON)
                        .body("""
                                {
                                    "networkId": "id",
                                    "networkUuid": "%s"
                                }
                            """
                            .formatted(networkUUID)));

        // Stop test at getNetworkValuesFromExistingNetwork
        Mockito.doReturn(null).when(networkService).getNetworkValuesFromExistingNetwork(networkUUID);
        // Upload network
        mvc.perform(MockMvcRequestBuilders.multipart("/network/new").file(file))
                .andExpect(status().isOk());

        Mockito.verify(networkService, times(1)).getNetworkValuesFromExistingNetwork(networkUUID);

        List<NetworkEntity> savedNetworks = networkRepository.findAll();
        assertEquals(1, savedNetworks.size());
        NetworkEntity expectedEntity = new NetworkEntity(networkUUID, "test.iidm");
        NetworkEntity actualEntity = savedNetworks.get(0);
        assertTrue(expectedEntity.getNetworkId().equals(actualEntity.getNetworkId()) && expectedEntity.getNetworkName().equals(actualEntity.getNetworkName()));

    }

    private String network(UUID id, String name) {
        return """
                {
                    "networkId": "%s",
                    "networkName": "%s"
                }
            """
            .formatted(id, name);
    }

    @Test
    public void getTest() throws Exception {
        UUID id1 = UUID.randomUUID();
        String name1 = "test1.iidm";
        UUID id2 = UUID.randomUUID();
        String name2 = "test1.iidm";
        networkRepository.save(new NetworkEntity(id1, name1));
        networkRepository.save(new NetworkEntity(id2, name2));

        mvc.perform(get("/network/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + network(id1, name1) + ", " + network(id2, name2) + "]", true));

    }

    @Test
    public void idTest() throws Exception {
        UUID networkUUID = UUID.randomUUID();

        Network testNetwork = NetworkTest1Factory.create();
        Mockito.when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenReturn(testNetwork);

        MvcResult mvcResult = mvc.perform(get("/network/" + networkUUID + "/values")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String resultNetworkValuesJson = mvcResult.getResponse().getContentAsString();
        NetworkValues resultNetworkValues = objectMapper.readValue(resultNetworkValuesJson, NetworkValues.class);
        resultNetworkValuesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNetworkValues);

        String networkValuesJson = new String(getClass().getResourceAsStream(TEST_DATA_DIR + RESOURCE_PATH_DELIMITER + "network/networkValues.json").readAllBytes());
        NetworkValues networkValues = objectMapper.readValue(networkValuesJson, NetworkValues.class);
        networkValues.setNetworkId(networkUUID);

        String expectNetworkValuesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(networkValues);
        LOGGER.info("expect network values = " + expectNetworkValuesJson);
        LOGGER.info("result network values = " + resultNetworkValuesJson);

        assertEquals(objectMapper.readTree(expectNetworkValuesJson), objectMapper.readTree(resultNetworkValuesJson));

        Mockito.verify(networkService, times(1)).getNetworkValuesFromExistingNetwork(networkUUID);

    }

    @Test
    public void unknownNetworkTest() throws Exception {
        UUID networkUUID = UUID.randomUUID();

        Mockito.when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenThrow(new PowsyblException());

        mvc.perform(MockMvcRequestBuilders.get("/network/" + networkUUID + "/values")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(networkStoreService, times(1)).getNetwork(networkUUID, PreloadingStrategy.COLLECTION);

    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForStaticVarCompensatorTest"
    })
    void ruleMatchingTest(Network testNetwork, String ruleToMatchFile, int ruleIndex, List<String> expectedMatchedIds) throws Exception {
        UUID networkUUID = UUID.randomUUID();

        Mockito.when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenReturn(testNetwork);

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
