package org.gridsuite.mapping.server.dto.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.FilterEmbeddable;
import org.gridsuite.mapping.server.utils.PropertyType;
import org.gridsuite.mapping.server.utils.Methods;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BooleanFilter extends AbstractFilter {

    private boolean value;

    @Override
    public FilterEmbeddable convertFilterToEntity(UUID ruleId) {
        FilterEmbeddable convertedFilter = new FilterEmbeddable();
        convertedFilter.setFilterId(this.getFilterId());
        convertedFilter.setRuleId(ruleId);
        convertedFilter.setType(PropertyType.BOOLEAN);
        convertedFilter.setProperty(this.getProperty());
        convertedFilter.setOperand(this.getOperand());
        convertedFilter.setValue(Methods.convertBooleanToString(value));
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
        }
        return String.format("equipment.%s %s %b", this.getProperty(), stringOperand, value);
    }
}
