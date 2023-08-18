/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.SetGroupType;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Getter
@Setter
public class ModelSetsGroupId implements Serializable {

    private String name;

    private String modelName;

    private SetGroupType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelSetsGroupId modelSetsGroupIdClass = (ModelSetsGroupId) o;
        return name.equals(modelSetsGroupIdClass.name) &&
                modelName.equals(modelSetsGroupIdClass.modelName) &&
                type.equals(modelSetsGroupIdClass.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, modelName, type);
    }

}
