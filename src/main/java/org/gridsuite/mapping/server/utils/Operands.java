/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.utils;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public enum Operands {
    // Common
    EQUALS,
    NOT_EQUALS,
    // Number
    LOWER,
    LOWER_OR_EQUALS,
    HIGHER_OR_EQUALS,
    HIGHER,
    // String
    INCLUDES,
    STARTS_WITH,
    ENDS_WITH,
    // Enum
    IN,
    NOT_IN
}
