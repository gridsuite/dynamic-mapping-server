/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EnumFilter extends AbstractFilter {

    private List<String> value;

    @Override
    public FilterEntity convertFilterToEntity(RuleEntity rule) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(rule.getRuleId());
        convertedFilter.setRule(rule);
        convertedFilter.setType(PropertyType.ENUM);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(Methods.convertListToString(value));
        return convertedFilter;
    }

    @Override
    public String convertFilterToString() {
        String operand = "";
        String notPrefix = "";
        String template = "%sequipment.%s.name().%s(%s)";
        String multiTemplate = "%s%s.%s(equipment.%s.name())";
        boolean checkFirstValueOnly = false;

        String equalsOperand = "equals";
        String containsOperand = "contains";

        switch (this.getOperand()) {
            case EQUALS:
                operand = equalsOperand;
                checkFirstValueOnly = true;
                break;
            case NOT_EQUALS:
                operand = equalsOperand;
                notPrefix = "!";
                checkFirstValueOnly = true;
                break;
            case IN:
                operand = containsOperand;
                template = multiTemplate;
                break;
            case NOT_IN:
                operand = containsOperand;
                notPrefix = "!";
                template = multiTemplate;
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Operand");
        }
        List<String> escapedValues = value.stream().map(unitValue -> String.format("\"%s\"", unitValue.replace("\"", "\\\""))).collect(Collectors.toList());
        if (checkFirstValueOnly) {
            return String.format(template, notPrefix, this.getProperty(), operand, escapedValues.get(0));
        } else {
            return String.format(template, notPrefix, escapedValues, operand, this.getProperty());

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
            default:
                break;
        }
        return isMatched;
    }
}
