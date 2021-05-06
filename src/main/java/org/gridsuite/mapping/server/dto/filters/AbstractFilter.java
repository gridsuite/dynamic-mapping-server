package org.gridsuite.mapping.server.dto.filters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.gridsuite.mapping.server.model.FilterEntity;
import org.gridsuite.mapping.server.utils.Operands;
import org.gridsuite.mapping.server.utils.Methods;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EnumFilter.class, name = "ENUM"),
        @JsonSubTypes.Type(value = NumberFilter.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = StringFilter.class, name = "STRING"),
        @JsonSubTypes.Type(value = BooleanFilter.class, name = "BOOLEAN")})
@Data
public abstract class AbstractFilter {
    private String filterId;
    private String property;
    private Operands operand;

    public static AbstractFilter createFilterFromEntity(FilterEntity filterEntity) {
        AbstractFilter filter = new StringFilter();
        switch (filterEntity.getType()) {
            case BOOLEAN:
                BooleanFilter booleanFilter = new BooleanFilter();
                booleanFilter.setFilterId(filterEntity.getFilterId());
                booleanFilter.setProperty(filterEntity.getProperty());
                booleanFilter.setOperand(filterEntity.getOperand());
                booleanFilter.setValue(Methods.convertStringToBoolean(filterEntity.getValue()));
                filter = booleanFilter;
                break;
            case ENUM:
                EnumFilter enumFilter = new EnumFilter();
                enumFilter.setFilterId(filterEntity.getFilterId());
                enumFilter.setProperty(filterEntity.getProperty());
                enumFilter.setOperand(filterEntity.getOperand());
                enumFilter.setValue(Methods.convertStringToList(filterEntity.getValue()));
                filter = enumFilter;
                break;

            case NUMBER:
                NumberFilter numberFilter = new NumberFilter();
                numberFilter.setFilterId(filterEntity.getFilterId());
                numberFilter.setProperty(filterEntity.getProperty());
                numberFilter.setOperand(filterEntity.getOperand());
                numberFilter.setValue(Methods.convertStringToNumber(filterEntity.getValue()));
                filter = numberFilter;
                break;
            case STRING:
                StringFilter stringFilter = new StringFilter();
                stringFilter.setFilterId(filterEntity.getFilterId());
                stringFilter.setProperty(filterEntity.getProperty());
                stringFilter.setOperand(filterEntity.getOperand());
                stringFilter.setValue(filterEntity.getValue());
                filter = stringFilter;
                break;
        }
        return filter;
    }

    public abstract FilterEntity convertFilterToEntity(UUID ruleId);

    public abstract String convertFilterToString();
}
