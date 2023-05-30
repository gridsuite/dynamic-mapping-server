/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.gridsuite.mapping.server.dto.automata.extensions.EntityProperty;
import org.gridsuite.mapping.server.utils.AutomatonFamily;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "family", visible = true)
@JsonSubTypes({ })
@Data
@NoArgsConstructor
public abstract class AbstractAutomaton {
    @Schema(description = "Automaton family")
    @JsonProperty
    @EntityProperty
    private AutomatonFamily family;

    @Schema(description = "Mapped Model Instance ID")
    @EntityProperty
    private String model;

    @Schema(description = "Mapped Parameters Set Group ID")
    @EntityProperty
    private String setGroup;

    @JsonIgnore
    public abstract String getExportedId();

    @JsonIgnore
    public abstract String getExportedClassName();

    public abstract List<BasicProperty> convertToBasicProperties();

}

