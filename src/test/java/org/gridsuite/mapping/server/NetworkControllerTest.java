/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
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
import java.util.UUID;

import static org.gridsuite.mapping.server.MappingConstants.CASE_API_VERSION;
import static org.gridsuite.mapping.server.MappingConstants.NETWORK_CONVERSION_API_VERSION;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
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
    private MockMvc mvc;
    private MockRestServiceServer mockServer;

    @SpyBean
    NetworkService networkService;

    @MockBean
    private NetworkStoreService networkStoreService;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    String caseApiUri = "http://case-server/";
    String networkConversionApiUri = "http://network-conversion-server/";

    @Test
    public void fileTest() throws Exception {

        UUID caseUUID = UUID.randomUUID();
        UUID networkUUID = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "network.iidm",
                MediaType.TEXT_PLAIN_VALUE,
                "This is a network".getBytes());

        // Mock call to case-server for import
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(caseApiUri + CASE_API_VERSION + "/cases/private")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(TEXT_PLAIN)
                        .body("\"" + caseUUID + "\""));

        // Mock call to case-server for conversion
        mockServer.expect(ExpectedCount.once(), requestTo(new URI(networkConversionApiUri + NETWORK_CONVERSION_API_VERSION + "/networks?caseUuid=" + caseUUID)))
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
    }

    @Test
    public void idTest() throws Exception {
        UUID networkUUID = UUID.randomUUID();

        Network testNetwork = NetworkTest1Factory.create();
        Mockito.when(networkStoreService.getNetwork(networkUUID, PreloadingStrategy.COLLECTION)).thenReturn(testNetwork);

        String expectedResult = "[\n" +
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
                "]";

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
}