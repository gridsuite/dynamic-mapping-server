/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import org.gridsuite.mapping.server.utils.StringListConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TapChangerBlockingAutomaton extends AbstractAutomaton {

    public static final String MODEL_CLASS = "TapChangerBlockingAutomaton";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_U_MEASUREMENTS = "uMeasurements";
    public static final String PROPERTY_TRANSFORMERS = "transformers";

    @Schema(description = "Name property")
    @JsonProperty(PROPERTY_NAME)
    @EntityProperty(value = PROPERTY_NAME, meta = true)
    private String name;

    @Schema(description = "U Measurement property")
    @JsonProperty(PROPERTY_U_MEASUREMENTS)
    @EntityProperty(value = PROPERTY_U_MEASUREMENTS, meta = true, converter = StringListConverter.class)
    private List<String> uMeasurements;

    @Schema(description = "Transformers property")
    @JsonProperty(PROPERTY_TRANSFORMERS)
    @EntityProperty(value = PROPERTY_TRANSFORMERS, meta = true, converter = StringListConverter.class)
    private List<String> transformers;

    @Override
    public String getExportedId() {
        return name;
    }

    @Override
    public String getExportedClassName() {
        return MODEL_CLASS;
    }

    @Override
    public ArrayList<BasicProperty> convertToBasicProperties() {
        ArrayList<BasicProperty> properties = new ArrayList<>();
        properties.add(new BasicProperty(PROPERTY_U_MEASUREMENTS,
                uMeasurements.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", "))));
        properties.add(new BasicProperty(PROPERTY_TRANSFORMERS,
                transformers.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", "))));
        return properties;
    }
}
