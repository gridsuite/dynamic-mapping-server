/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@NoArgsConstructor
@AllArgsConstructor
public class FilterId implements Serializable {

    private String filterId;
    private UUID ruleId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilterId filterIdClass = (FilterId) o;
        return filterId.equals(filterIdClass.filterId) &&
                ruleId.equals(filterIdClass.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterId, ruleId);
    }

}
