/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.plugins.automaton;

import java.util.Map;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public final class AutomatonPluggableTypes {
    // declare all automaton pluggable types through this map
    public static final Map<String, Class<?>> TYPES = Map.of(
            "VOLTAGE", TapChangerBlockingAutomaton.class
    );

    private AutomatonPluggableTypes() {
    }
}
