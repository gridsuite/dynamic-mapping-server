/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.utils;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public interface AttributeConverter<D, E> {

    D toDtoAttribute(E entityAttribute);

    E toEntityAttribute(D dtoAttribute);
}
