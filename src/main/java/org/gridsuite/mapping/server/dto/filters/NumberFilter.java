package org.gridsuite.mapping.server.dto.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.FilterEmbeddable;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.gridsuite.mapping.server.utils.methods;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class NumberFilter extends Filter {

    private float value;

    @Override
    public FilterEmbeddable convertFilterToEntity(UUID ruleId) {
        FilterEmbeddable convertedFilter = new FilterEmbeddable();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(ruleId);
        convertedFilter.setType(PropertyType.NUMBER);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(methods.convertNumberToString(value));
        return convertedFilter;
    };

    public String convertFilterToString() {
        String stringOperand = "";
        switch(this.getOperand()) {
            case EQUALS:
                stringOperand = "==";
                break;
            case NOT_EQUALS:
                stringOperand = "!=";
                break;
            case LOWER:
                stringOperand = "<";
                break;
            case LOWER_OR_EQUALS:
                stringOperand = "<=";
                break;
            case HIGHER_OR_EQUALS:
                stringOperand = ">=";
                break;
            case HIGHER:
                stringOperand = ">";
                break;
        }
        return String.format("equipment.%s %s %f", this.getProperty(), stringOperand, value);
    };
}
