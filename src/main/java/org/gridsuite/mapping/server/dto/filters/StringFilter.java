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
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.Methods;
import org.gridsuite.mapping.server.utils.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StringFilter extends AbstractFilter {

    private ArrayList<String> value;

    @Override
    public FilterEntity convertFilterToEntity(RuleEntity rule) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(rule.getRuleId());
        convertedFilter.setRule(rule);
        convertedFilter.setType(PropertyType.STRING);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(Methods.convertListToString(value));
        return convertedFilter;
    }

    public String convertFilterToString() {
        String stringOperand = "";
        String notPrefix = "";
        String template = "%sequipment.%s.%s(%s)";
        boolean checkFirstValueOnly = false;

        switch (this.getOperand()) {
            case EQUALS:
                stringOperand = "equals";
                checkFirstValueOnly = true;
                break;
            case NOT_EQUALS:
                stringOperand = "equals";
                notPrefix = "!";
                checkFirstValueOnly = true;
                break;
            case STARTS_WITH:
                stringOperand = "startsWith";
                checkFirstValueOnly = true;

                break;
            case INCLUDES:
                stringOperand = "contains";
                checkFirstValueOnly = true;
                break;
            case ENDS_WITH:
                stringOperand = "endsWith";
                checkFirstValueOnly = true;
                break;
            case IN:
                stringOperand = "contains";
                template = "%s%s.%s(equipment.%s)";
                break;
            case NOT_IN:
                stringOperand = "contains";
                notPrefix = "!";
                template = "%s%s.%s(equipment.%s)";
                break;
        }
        // Need to escape string values;
        List<String> escapedValues = value.stream().map(value -> String.format("\"%s\"", value.replaceAll("\"", "\\\""))).collect(Collectors.toList());
        if (checkFirstValueOnly) {
            return String.format(template, notPrefix, this.getProperty(), stringOperand, escapedValues.get(0));
        } else {
            return String.format(template, notPrefix, escapedValues, stringOperand, this.getProperty());

        }

    }
}
