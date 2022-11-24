/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.utils;

import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */

public interface AutomatonIdProvider {
    String getId(AbstractAutomaton automaton);
}
