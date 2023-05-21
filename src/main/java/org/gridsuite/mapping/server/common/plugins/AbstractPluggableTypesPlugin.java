/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.common.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.*;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public abstract class AbstractPluggableTypesPlugin<D, E> extends SimpleModule implements PluggableTypesPlugin<D, E> {

    protected AbstractPluggableTypesPlugin(ObjectMapper objectMapper) {
        super();
        registerTypes(objectMapper);
    }

    private void registerTypes(ObjectMapper objectMapper) {

        // Create a list to hold all the subtypes
        List<NamedType> pluggableTypes = new ArrayList<>();

        // Add the additional subtype
        getPluggableTypes().forEach((k, v) -> pluggableTypes.add(new NamedType(v, k)));

        // Register the merged subtypes with the ObjectMapper
        objectMapper
                .getSubtypeResolver()
                .registerSubtypes(pluggableTypes.toArray(new NamedType[0]));
    }
}
