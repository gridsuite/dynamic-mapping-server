package org.gridsuite.mapping.server.dto.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.FilterEmbeddable;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.gridsuite.mapping.server.utils.methods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class EnumFilter extends Filter {

    private ArrayList<String> value;

    @Override
    public FilterEmbeddable convertFilterToEntity(UUID ruleId) {
        FilterEmbeddable convertedFilter = new FilterEmbeddable();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(ruleId);
        convertedFilter.setType(PropertyType.ENUM);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(methods.convertListToString(value));
        return convertedFilter;
    };


    public String convertFilterToString() {
        String stringOperand = "";
        String notPrefix = "";
        switch(this.getOperand()) {
            case EQUALS:
                stringOperand = "equals";
                break;
            case NOT_EQUALS:
                stringOperand = "equals";
                notPrefix = "!";
                break;
            case IN:
                stringOperand = "contains";
                break;
            case NOT_IN:
                stringOperand = "contains";
                notPrefix = "!";
                break;
        }

        List<String> escapedValues = value.stream().map(value -> String.format("\"%s\"", value)).collect(Collectors.toList());

        return String.format("%sequipment.%s.%s(%s)", notPrefix,this.getProperty(), stringOperand, escapedValues);
    };

}
