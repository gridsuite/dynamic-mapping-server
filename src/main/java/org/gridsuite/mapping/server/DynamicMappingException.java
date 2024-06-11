/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public class DynamicMappingException extends RuntimeException {

    public enum Type {
        URI_SYNTAX,
        MAPPING_NAME_NOT_PROVIDED,
        GET_FILTER_ERROR,
        CREATE_FILTER_ERROR,
        UPDATE_FILTER_ERROR,
        DUPLICATE_FILTER_ERROR,
        DELETE_FILTER_ERROR,
        FILTER_NOT_FOUND
    }

    private final Type type;

    public DynamicMappingException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
