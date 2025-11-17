/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import jakarta.persistence.Embeddable;
import lombok.*;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.io.Serializable;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Getter
@Setter
public class ModelSetsGroupId implements Serializable {

    private String name;

    private String modelId;

    private SetGroupType type;

}
