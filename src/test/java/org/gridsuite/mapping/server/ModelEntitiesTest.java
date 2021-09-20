/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.model.ModelParameterDefinitionId;
import org.gridsuite.mapping.server.model.ModelParameterId;
import org.gridsuite.mapping.server.model.ModelParameterSetId;
import org.gridsuite.mapping.server.model.ModelSetsGroupId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ModelEntitiesTest {
    @Test
    public void parameterDefinitionIdTestEqualHashCode() {
        ModelParameterDefinitionId id = new ModelParameterDefinitionId("name", "modelName");
        ModelParameterDefinitionId id2 = new ModelParameterDefinitionId("name", "modelName");

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }

    @Test
    public void parameterIdTestEqualHashCode() {
        ModelParameterId id = new ModelParameterId("name", "modelName", "setName");
        ModelParameterId id2 = new ModelParameterId("name", "modelName", "setName");

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }

    @Test
    public void parameterSetIdTestEqualHashCode() {
        ModelParameterSetId id = new ModelParameterSetId("name", "groupName", "modelName");
        ModelParameterSetId id2 = new ModelParameterSetId("name", "groupName", "modelName");

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }

    @Test
    public void setsGroupIdTestEqualHashCode() {
        ModelSetsGroupId id = new ModelSetsGroupId("name", "modelName");
        ModelSetsGroupId id2 = new ModelSetsGroupId("name", "modelName");

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }
}
