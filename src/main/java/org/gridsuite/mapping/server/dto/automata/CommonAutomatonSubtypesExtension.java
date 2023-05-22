/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import com.google.auto.service.AutoService;
import org.gridsuite.mapping.server.dto.automata.plugins.AutomatonSubtypes;
import org.gridsuite.mapping.server.dto.automata.plugins.AutomatonSubtypesExtension;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@AutomatonSubtypes({
        @AutomatonSubtypes.Type(value = CurrentLimitAutomaton.class, name = "CURRENT_LIMIT"),
        @AutomatonSubtypes.Type(value = TapChangerBlockingAutomaton.class, name = "VOLTAGE")
})
@AutoService(AutomatonSubtypesExtension.class)
public class CommonAutomatonSubtypesExtension implements AutomatonSubtypesExtension {
}
