/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.utils;

import java.util.List;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    @Override
    public List<String> toDtoAttribute(String entityAttribute) {
        return entityAttribute != null ? Methods.convertStringToList(entityAttribute) : null;
    }

    @Override
    public String toEntityAttribute(List<String> dtoAttribute) {
        return dtoAttribute != null ? Methods.convertListToString(dtoAttribute) : null;
    }
}
