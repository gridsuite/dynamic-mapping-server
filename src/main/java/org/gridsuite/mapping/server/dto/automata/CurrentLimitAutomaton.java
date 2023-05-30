/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.dto.automata.extensions.EntityProperty;

import java.util.ArrayList;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CurrentLimitAutomaton extends AbstractAutomaton {

    public static final String MODEL_CLASS = "CurrentLimitAutomaton";

    public static final String PROPERTY_WATCHED_ELEMENT = "watchedElement";
    public static final String PROPERTY_SIDE = "side";
    public static final String PROPERTY_STATIC_ID = "staticId";

    @Schema(description = "Element watched by the automaton")
    @JsonProperty(PROPERTY_WATCHED_ELEMENT)
    @EntityProperty(value = PROPERTY_WATCHED_ELEMENT, meta = true)
    private String watchedElement;

    @Schema(description = "Side of the automaton")
    @JsonProperty(PROPERTY_SIDE)
    @EntityProperty(value = PROPERTY_SIDE, meta = true)
    private String side;

    @Override
    public String getExportedId() {
        return String.format("%s_%s", this.getModel(), watchedElement);
    }

    @Override
    public String getExportedClassName() {
        return MODEL_CLASS;
    }

    @Override
    public ArrayList<BasicProperty> convertToBasicProperties() {
        ArrayList<BasicProperty> properties = new ArrayList<>();
        properties.add(new BasicProperty(PROPERTY_STATIC_ID, "\"" + watchedElement + "\""));
        properties.add(new BasicProperty(PROPERTY_SIDE, side));

        return properties;
    }
}


