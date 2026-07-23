/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.utils;

import org.gridsuite.mapping.server.model.ModelVariableDefinitionEntity;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
class PropertyUtilsTest {
    @Test
    void testCopyNonNullProperties() {
        // test with a ModelVariableDefinitionEntity
        ModelVariableDefinitionEntity variableDefinitionEntity = new ModelVariableDefinitionEntity(UUID.randomUUID(), "load_running_value", VariableType.DOUBLE, "KW", 100.0, null, null, new Date(),
                new Date());
        ModelVariableDefinitionEntity variableDefinitionEntity2 = new ModelVariableDefinitionEntity(UUID.randomUUID(), "load_running_value2", VariableType.BOOL, null, 90.0, null, null, null,
                new Date());

        // call method to be tested
        PropertyUtils.copyNonNullProperties(variableDefinitionEntity2, variableDefinitionEntity);

        // Check result
        // name must be changed
        assertEquals("load_running_value2", variableDefinitionEntity.getName());
        // type must be changed
        assertEquals(VariableType.BOOL, variableDefinitionEntity.getType());
        // unit must be not changed
        assertEquals("KW", variableDefinitionEntity.getUnit());

        // call method to be tested with options
        PropertyUtils.copyNonNullProperties(variableDefinitionEntity2, variableDefinitionEntity, "unit");
        // unit must be null
        assertNull(variableDefinitionEntity.getUnit());

    }
}
