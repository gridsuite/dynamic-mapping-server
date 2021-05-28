/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.FilterEntity;
import org.gridsuite.mapping.server.utils.PropertyType;

import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StringFilter extends AbstractFilter {

    private String value;

    @Override
    public FilterEntity convertFilterToEntity(UUID ruleId) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(ruleId);
        convertedFilter.setType(PropertyType.STRING);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(value);
        return convertedFilter;
    }

    public String convertFilterToString() {
        String stringOperand = "";
        String notPrefix = "";
        switch (this.getOperand()) {
            case EQUALS:
                stringOperand = "equals";
                break;
            case NOT_EQUALS:
                stringOperand = "equals";
                notPrefix = "!";
                break;
            case STARTS_WITH:
                stringOperand = "startsWith";
                break;
            case INCLUDES:
                stringOperand = "contains";
                break;
            case ENDS_WITH:
                stringOperand = "endsWith";
                break;
        }
        // Need to escape string values;
        return String.format("%sequipment.%s.%s(\"%s\")", notPrefix, this.getProperty(), stringOperand, value);
    }
}
