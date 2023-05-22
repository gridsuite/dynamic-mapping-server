/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.dto.automata.extensions;

import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutomatonSubtypes {
    Type[] value();
    @interface Type {
        Class<? extends AbstractAutomaton> value();
        String name() default "";
    }
}
