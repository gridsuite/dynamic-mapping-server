/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.service.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.gridsuite.mapping.server.MappingApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextHierarchy({@ContextConfiguration(classes = {MappingApplication.class})})
public abstract class AbstractWireMockRestClientTest {

    public static final String ERROR_MESSAGE = "Something wrong in remote server";
    public static final String ERROR_MESSAGE_JSON = """
            {"message": "%s"}
        """.formatted(ERROR_MESSAGE);

    public final Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    protected WireMockServer wireMockServer;

    protected String initMockWebServer(WireMockServer server) {
        wireMockServer = server;

        wireMockServer.start();
        getLogger().info("Mock server started at port {}", wireMockServer.port());

        // get base URL
        return wireMockServer.baseUrl();
    }

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {
        try {
            wireMockServer.shutdown();
        } catch (Exception e) {
            getLogger().info("Can not shutdown the mock server {}", this.getClass().getSimpleName());
        }
    }
}
