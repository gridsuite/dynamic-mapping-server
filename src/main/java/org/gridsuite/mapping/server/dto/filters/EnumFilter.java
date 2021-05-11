package org.gridsuite.mapping.server.dto.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.FilterEntity;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.gridsuite.mapping.server.utils.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EnumFilter extends AbstractFilter {

    private ArrayList<String> value;

    @Override
    public FilterEntity convertFilterToEntity(UUID ruleId) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(ruleId);
        convertedFilter.setType(PropertyType.ENUM);
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

        List<String> escapedValues = value.stream().map(value -> String.format("\"%s\"", value)).collect(Collectors.toList());
        if (checkFirstValueOnly) {
            return String.format(template, notPrefix, this.getProperty(), stringOperand, escapedValues.get(0));
        } else {
            return String.format(template, notPrefix, escapedValues, stringOperand, this.getProperty());

        }
    }
}
