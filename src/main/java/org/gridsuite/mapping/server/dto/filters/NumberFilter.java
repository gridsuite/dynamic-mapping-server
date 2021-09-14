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
import org.gridsuite.mapping.server.utils.PropertyType;
import org.gridsuite.mapping.server.utils.Methods;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class NumberFilter extends AbstractFilter {

    private ArrayList<Float> value;

    @Override
    public FilterEntity convertFilterToEntity(RuleEntity rule) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(rule.getRuleId());
        convertedFilter.setRule(rule);
        convertedFilter.setType(PropertyType.NUMBER);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(Methods.convertNumberListToString(value));
        return convertedFilter;
    }

    public String convertFilterToString() {
        String stringOperand = "";
        String notPrefix = "";
        String template = "equipment.%s %s %f";
        boolean checkFirstValueOnly = false;
        switch (this.getOperand()) {
            case EQUALS:
                stringOperand = "==";
                checkFirstValueOnly = true;
                break;
            case NOT_EQUALS:
                stringOperand = "!=";
                checkFirstValueOnly = true;
                break;
            case LOWER:
                stringOperand = "<";
                checkFirstValueOnly = true;
                break;
            case LOWER_OR_EQUALS:
                stringOperand = "<=";
                checkFirstValueOnly = true;
                break;
            case HIGHER_OR_EQUALS:
                stringOperand = ">=";
                checkFirstValueOnly = true;
                break;
            case HIGHER:
                stringOperand = ">";
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
        if (checkFirstValueOnly) {
            return String.format(Locale.US, template, this.getProperty(), stringOperand, value.get(0));
        } else {
            return String.format(Locale.US, template, notPrefix, value, stringOperand, this.getProperty());

        }
    }
}
