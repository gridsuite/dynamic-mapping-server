/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.utils.assertions;

import org.assertj.core.util.CheckReturnValue;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.ParameterFile;
import org.gridsuite.mapping.server.dto.models.ParametersSet;

import java.util.List;

/**
 *  @author Tristan Chuine <tristan.chuine at rte-france.com>
 * {@link org.assertj.core.api.Assertions Assertions} completed with our custom assertions classes.
 */
public class Assertions extends org.assertj.core.api.Assertions {
    @CheckReturnValue
    public static <T extends InputMapping> DTOAssert<InputMapping> assertThat(T actual) {
        return new DTOAssert<>(actual);
    }

    public static <T extends ParametersSet> DTOAssert<ParametersSet> assertThat(T actual) {
        return new DTOAssert<>(actual);
    }

    public static <T extends ParameterFile> DTOAssert<ParameterFile> assertThat(T actual) {
        return new DTOAssert<>(actual);
    }

    @CheckReturnValue
    public static <T extends ExpertFilter> DTOAssert<ExpertFilter> assertThat(T actual) {
        return new DTOAssert<>(actual);
    }

    @CheckReturnValue
    public static DTOAssert<List<?>> assertThatDtoList(List<?> actual) {
        return new DTOAssert<>(actual);
    }

}
