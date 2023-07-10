/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.model.ModelParameterId;
import org.gridsuite.mapping.server.model.ModelParameterSetId;
import org.gridsuite.mapping.server.model.ModelSetsGroupId;
import org.gridsuite.mapping.server.utils.SetGroupType;
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
    public void parameterIdTestEqualHashCode() {
        ModelParameterId id = new ModelParameterId("name", "modelName", "setName", "groupName", SetGroupType.FIXED);
        ModelParameterId id2 = new ModelParameterId("name", "modelName", "setName", "groupName", SetGroupType.FIXED);

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }

    @Test
    public void parameterSetIdTestEqualHashCode() {
        ModelParameterSetId id = new ModelParameterSetId("name", "groupName", "modelName", SetGroupType.FIXED);
        ModelParameterSetId id2 = new ModelParameterSetId("name", "groupName", "modelName", SetGroupType.FIXED);

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }

    @Test
    public void setsGroupIdTestEqualHashCode() {
        ModelSetsGroupId id = new ModelSetsGroupId("name", "modelName", SetGroupType.FIXED);
        ModelSetsGroupId id2 = new ModelSetsGroupId("name", "modelName", SetGroupType.FIXED);

        assertEquals(id, id);
        assertEquals(id, id2);
        assertNotEquals(null, id);
        assertNotEquals("id", id);
    }
}
