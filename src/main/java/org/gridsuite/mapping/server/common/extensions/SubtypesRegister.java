/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.common.extensions;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public interface SubtypesRegister<D, E> {
    Map<String, Class<?>> getSubtypes();

    D fromEntity(E entity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
}
