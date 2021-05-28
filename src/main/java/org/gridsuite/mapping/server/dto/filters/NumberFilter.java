package org.gridsuite.mapping.server.dto.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.FilterEntity;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.gridsuite.mapping.server.utils.Methods;

import java.util.Locale;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class NumberFilter extends AbstractFilter {

    private float value;

    @Override
    public FilterEntity convertFilterToEntity(UUID ruleId) {
        FilterEntity convertedFilter = new FilterEntity();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(ruleId);
        convertedFilter.setType(PropertyType.NUMBER);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(Methods.convertNumberToString(value));
        return convertedFilter;
    }

    public String convertFilterToString() {
        String stringOperand = "";
        switch (this.getOperand()) {
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
        return String.format(Locale.US, "equipment.%s %s %f", this.getProperty(), stringOperand, value);
    }
}
