/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.mapping.server.model.NetworkEntity;
import org.gridsuite.mapping.server.repository.NetworkRepository;
import org.gridsuite.mapping.server.service.NetworkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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
public class NetworkControllerTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @SpyBean
    NetworkService networkService;

    @MockBean
    private NetworkStoreService networkStoreService;

    @Before
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
                        .body("{\"networkId\": \"id \", \"networkUuid\":\"" + networkUUID + "\"}"));

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

        mvc.perform(MockMvcRequestBuilders.delete("/network/" + networkUUID)).andExpect(status().isOk());
        savedNetworks = networkRepository.findAll();
        assertEquals(0, savedNetworks.size());

    }

    private String network(UUID id, String name) {
        return "{\n" +
                "  \"networkId\": \"" + id + "\",\n" +
                "  \"networkName\": \"" + name + "\"\n" +
                "}";
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

        String expectedResult = "{\n" +
                "\"networkId\": \"" + networkUUID + "\",\n" +
                "\"propertyValues\": [\n" +
                "  {\n" +
                "    \"type\": \"GENERATOR\",\n" +
                "    \"values\": {\n" +
                "      \"terminal.voltageLevel.nominalV\": [\n" +
                "        \"400.0\"\n" +
                "      ],\n" +
                "      \"terminal.voltageLevel.substation.country.name\": [\n" +
                "        \"FRANCE\"\n" +
                "      ],\n" +
                "      \"id\": [\n" +
                "        \"generator1\"\n" +

                "      ],\n" +
                "      \"energySource\": [\n" +
                "        \"NUCLEAR\"\n" +
                "      ],\n" +
                "      \"voltageRegulatorOn\": [\n" +
                "        \"true\"\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"LOAD\",\n" +
                "    \"values\": {\n" +
                "      \"loadType\": [\n" +
                "        \"UNDEFINED\"\n" +
                "      ],\n" +
                "      \"terminal.voltageLevel.nominalV\": [\n" +
                "        \"400.0\"\n" +
                "      ],\n" +
                "      \"terminal.voltageLevel.substation.country.name\": [\n" +
                "        \"FRANCE\"\n" +
                "      ],\n" +
                "      \"id\": [\n" +
                "        \"load1\"\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]\n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.get("/network/" + networkUUID + "/values")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResult, true));

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

    @Test
    public void ruleMatchingTest() throws Exception {
        UUID networkUUID = UUID.randomUUID();

        Network testNetwork = NetworkTest1Factory.create();
        Mockito.when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenReturn(testNetwork);

        int generatorIndex = 2;
        String generatorRuleToMatch = "{\n" +
                "  \"ruleIndex\": " + generatorIndex + ",\n" +
                "  \"equipmentType\": \"" + "GENERATOR" + "\",\n" +
                "  \"composition\": \"" + "(filter1 || filter2 || filter4 || filter5) && (filter3 || filter6 || filter7) || filter8" + "\",\n" +
                "  \"filters\": [\n" +
                "    {\n" +
                "      \"filterId\": \"filter1\",\n" +
                "      \"operand\": \"EQUALS\",\n" +
                "      \"property\": \"id\",\n" +
                "      \"value\": [\"generator1\"],\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter2\",\n" +
                "      \"operand\": \"HIGHER\",\n" +
                "      \"property\": \"terminal.voltageLevel.nominalV\",\n" +
                "      \"value\": [3.0],\n" +
                "      \"type\": \"NUMBER\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter3\",\n" +
                "      \"operand\": \"EQUALS\",\n" +
                "      \"property\": \"voltageRegulatorOn\",\n" +
                "      \"value\": true,\n" +
                "      \"type\": \"BOOLEAN\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter4\",\n" +
                "      \"operand\": \"INCLUDES\",\n" +
                "      \"property\": \"id\",\n" +
                "      \"value\": [\"generator\"],\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter5\",\n" +
                "      \"operand\": \"ENDS_WITH\",\n" +
                "      \"property\": \"id\",\n" +
                "      \"value\": [\"1\"],\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter6\",\n" +
                "      \"operand\": \"HIGHER_OR_EQUALS\",\n" +
                "      \"property\": \"terminal.voltageLevel.nominalV\",\n" +
                "      \"value\": [400],\n" +
                "      \"type\": \"NUMBER\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter7\",\n" +
                "      \"operand\": \"IN\",\n" +
                "      \"property\": \"terminal.voltageLevel.nominalV\",\n" +
                "      \"value\": [20,60,400],\n" +
                "      \"type\": \"NUMBER\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter8\",\n" +
                "      \"operand\": \"NOT_EQUALS\",\n" +
                "      \"property\": \"voltageRegulatorOn\",\n" +
                "      \"value\": true,\n" +
                "      \"type\": \"BOOLEAN\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/network/" + networkUUID + "/matches/rule")
                        .content(generatorRuleToMatch)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"ruleIndex\":" + generatorIndex + ",\"matchedIds\":[\"generator1\"]}", true));

        int loadIndex = 0;
        String loadRuleToMatch = "{\n" +
                "  \"ruleIndex\": " + loadIndex + ",\n" +
                "  \"equipmentType\": \"" + "LOAD" + "\",\n" +
                "  \"composition\": \"" + "filter1 || filter2 || filter3 || filter4 || filter5" + "\",\n" +
                "  \"filters\": [\n" +
                "    {\n" +
                "      \"filterId\": \"filter1\",\n" +
                "      \"operand\": \"NOT_IN\",\n" +
                "      \"property\": \"id\",\n" +
                "      \"value\": [\"load1\"],\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter2\",\n" +
                "      \"operand\": \"NOT_EQUALS\",\n" +
                "      \"property\": \"terminal.voltageLevel.substation.country.name\",\n" +
                "      \"value\": [\"FRANCE\"],\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter3\",\n" +
                "      \"operand\": \"STARTS_WITH\",\n" +
                "      \"property\": \"terminal.voltageLevel.substation.country.name\",\n" +
                "      \"value\": [\"GER\"],\n" +
                "      \"type\": \"STRING\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter4\",\n" +
                "      \"operand\": \"NOT_IN\",\n" +
                "      \"property\": \"terminal.voltageLevel.nominalV\",\n" +
                "      \"value\": [60, 225, 400],\n" +
                "      \"type\": \"NUMBER\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"filterId\": \"filter5\",\n" +
                "      \"operand\": \"LOWER_OR_EQUALS\",\n" +
                "      \"property\": \"terminal.voltageLevel.nominalV\",\n" +
                "      \"value\": [380],\n" +
                "      \"type\": \"NUMBER\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/network/" + networkUUID + "/matches/rule")
                        .content(loadRuleToMatch)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"ruleIndex\":" + loadIndex + ",\"matchedIds\":[]}", true));
    }
}
