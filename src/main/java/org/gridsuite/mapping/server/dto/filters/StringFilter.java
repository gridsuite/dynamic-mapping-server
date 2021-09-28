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
import org.gridsuite.mapping.server.utils.Operands;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StringFilter extends AbstractFilter {

    private List<String> value;

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

        String containsOperand = "contains";

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
                stringOperand = containsOperand;
                checkFirstValueOnly = true;
                break;
            case ENDS_WITH:
                stringOperand = "endsWith";
                checkFirstValueOnly = true;
                break;
            case IN:
                stringOperand = containsOperand;
                template = "%s%s.%s(equipment.%s)";
                break;
            case NOT_IN:
                stringOperand = containsOperand;
                notPrefix = "!";
                template = "%s%s.%s(equipment.%s)";
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Operand");
        }

        List<String> escapedValues = value.stream().map(unitValue -> String.format("\"%s\"", unitValue.replace("\"", "\\\""))).collect(Collectors.toList());
        if (checkFirstValueOnly) {
            return String.format(template, notPrefix, this.getProperty(), stringOperand, escapedValues.get(0));
        } else {
            return String.format(template, notPrefix, escapedValues, stringOperand, this.getProperty());

        }

    }

    @Override
    public boolean matchValueToFilter(String valueToTest) {
        boolean isMatched = false;

        switch (this.getOperand()) {
            case EQUALS:
            case IN:
            case NOT_IN:
            case NOT_EQUALS:
                boolean isNot = this.getOperand().equals(Operands.NOT_IN) || this.getOperand().equals(Operands.NOT_EQUALS);
                isMatched = isNot != value.stream().reduce(false, (acc, filterUniqueValue) -> acc || valueToTest.equals(filterUniqueValue), (a, b) -> a || b);
                break;
            case INCLUDES:
                isMatched = valueToTest.contains(value.get(0));
                break;
            case STARTS_WITH:
                isMatched = valueToTest.startsWith(value.get(0));
                break;
            case ENDS_WITH:
                isMatched = valueToTest.endsWith(value.get(0));
                break;
            default:
                break;
        }
        return isMatched;
    }
}

