/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.filters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.gridsuite.mapping.server.model.FilterEntity;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.Methods;
import org.gridsuite.mapping.server.utils.Operands;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NumberFilter.class, name = "NUMBER"),
    @JsonSubTypes.Type(value = StringFilter.class, name = "STRING"),
    @JsonSubTypes.Type(value = BooleanFilter.class, name = "BOOLEAN"),
    @JsonSubTypes.Type(value = EnumFilter.class, name = "ENUM")})
@Data
public abstract class AbstractFilter {
    private String filterId;
    private String property;
    private Operands operand;

    public static AbstractFilter createFilterFromEntity(FilterEntity filterEntity) {
        switch (filterEntity.getType()) {
            case BOOLEAN:
                BooleanFilter booleanFilter = new BooleanFilter();
                booleanFilter.setFilterId(filterEntity.getFilterId());
                booleanFilter.setProperty(filterEntity.getProperty());
                booleanFilter.setOperand(filterEntity.getOperand());
                booleanFilter.setValue(Methods.convertStringToBoolean(filterEntity.getValue()));
                return booleanFilter;
            case NUMBER:
                NumberFilter numberFilter = new NumberFilter();
                numberFilter.setFilterId(filterEntity.getFilterId());
                numberFilter.setProperty(filterEntity.getProperty());
                numberFilter.setOperand(filterEntity.getOperand());
                numberFilter.setValue(Methods.convertStringToNumberList(filterEntity.getValue()));
                return numberFilter;
            case STRING:
                StringFilter stringFilter = new StringFilter();
                stringFilter.setFilterId(filterEntity.getFilterId());
                stringFilter.setProperty(filterEntity.getProperty());
                stringFilter.setOperand(filterEntity.getOperand());
                stringFilter.setValue(Methods.convertStringToList(filterEntity.getValue()));
                return stringFilter;
            case ENUM:
                EnumFilter enumFilter = new EnumFilter();
                enumFilter.setFilterId(filterEntity.getFilterId());
                enumFilter.setProperty(filterEntity.getProperty());
                enumFilter.setOperand(filterEntity.getOperand());
                enumFilter.setValue(Methods.convertStringToList(filterEntity.getValue()));
                return enumFilter;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown filter type");
        }
    }

    public abstract FilterEntity convertFilterToEntity(RuleEntity rule);

    public abstract String convertFilterToString();

    public abstract boolean matchValueToFilter(String valueToTest);
}
