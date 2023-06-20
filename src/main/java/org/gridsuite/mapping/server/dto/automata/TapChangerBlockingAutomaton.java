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
import org.gridsuite.mapping.server.utils.Methods;
import org.gridsuite.mapping.server.utils.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private String name;

    @Schema(description = "U Measurement property")
    @JsonProperty(PROPERTY_U_MEASUREMENTS)
    private List<String> uMeasurements;

    @Schema(description = "Transformers property")
    @JsonProperty(PROPERTY_TRANSFORMERS)
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
    public ArrayList<BasicProperty> getExportedProperties() {
        ArrayList<BasicProperty> properties = new ArrayList<>();
        properties.add(new BasicProperty("uMeasurement",
                uMeasurements.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", ")), PropertyType.STRING));
        properties.add(new BasicProperty(PROPERTY_TRANSFORMERS,
                transformers.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", ")), PropertyType.STRING));
        return properties;
    }

    @Override
    public List<BasicProperty> toProperties() {
        ArrayList<BasicProperty> properties = new ArrayList<>();
        properties.add(new BasicProperty(PROPERTY_NAME, name, PropertyType.STRING));
        properties.add(new BasicProperty(PROPERTY_U_MEASUREMENTS,
                uMeasurements != null ? Methods.convertListToString(uMeasurements) : null, PropertyType.STRING));
        properties.add(new BasicProperty(PROPERTY_TRANSFORMERS,
                transformers != null ? Methods.convertListToString(transformers) : null, PropertyType.STRING));
        return properties;
    }

    @Override
    public void fromProperties(List<BasicProperty> properties) {
        Map<String, BasicProperty> propertiesMap = properties.stream()
                .collect(Collectors.toMap(BasicProperty::getName, elem -> elem));
        this.name = propertiesMap.get(PROPERTY_NAME).getValue();
        this.uMeasurements = Methods.convertStringToList(propertiesMap.get(PROPERTY_U_MEASUREMENTS).getValue());
        this.transformers = Methods.convertStringToList(propertiesMap.get(PROPERTY_TRANSFORMERS).getValue());
    }
}
