/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata.extensions;

import org.gridsuite.mapping.server.common.extensions.SubtypesRegister;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.model.AutomatonEntity;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public interface AutomatonSubtypesRegister extends SubtypesRegister<AbstractAutomaton, AutomatonEntity> {
}
