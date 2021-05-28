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
import org.gridsuite.mapping.server.utils.ParamsType;

import javax.persistence.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
@AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "params_id")),
        @AttributeOverride(name = "type", column = @Column(name = "params_type")),
})
public class ModelParamsEmbeddable {

    private String name;

    private ParamsType type;
}
