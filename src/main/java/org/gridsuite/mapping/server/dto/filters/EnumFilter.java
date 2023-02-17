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
import org.gridsuite.mapping.server.utils.Operands;
import org.gridsuite.mapping.server.utils.PropertyType;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EnumFilter extends AbstractFilter {

    private String value;

    @Override
    public FilterEntity convertFilterToEntity(RuleEntity rule) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(rule.getRuleId());
        convertedFilter.setRule(rule);
        convertedFilter.setType(PropertyType.ENUM);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(value);
        return convertedFilter;
    }

    @Override
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
        }
        return String.format("%sequipment.%s.toString().%s(\"%s\")", notPrefix, this.getProperty(), stringOperand, value);

    }

    @Override
    public boolean matchValueToFilter(String valueToTest) {
        boolean isNot = this.getOperand().equals(Operands.NOT_EQUALS);
        return isNot != (valueToTest.equals(value));
    }
}
