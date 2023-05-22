/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.common.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public abstract class AbstractSubtypesRegister<D, E> implements SubtypesRegister<D, E> {

    private final ObjectMapper objectMapper;

    protected AbstractSubtypesRegister(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    protected void registerSubtypes() {

        // collect subtypes
        List<NamedType> subtypes = new ArrayList<>();
        getSubtypes().forEach((k, v) -> subtypes.add(new NamedType(v, k)));

        // Register subtypes with the ObjectMapper
        objectMapper.getSubtypeResolver().registerSubtypes(subtypes.toArray(new NamedType[0]));
    }
}
