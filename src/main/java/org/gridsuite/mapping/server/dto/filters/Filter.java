package org.gridsuite.mapping.server.dto.filters;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.gridsuite.mapping.server.utils.Operands;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EnumFilter.class, name = "ENUM"),
        @JsonSubTypes.Type(value = NumberFilter.class, name = "NUMBER"),
        @JsonSubTypes.Type(value = StringFilter.class, name = "STRING"),
        @JsonSubTypes.Type(value = BooleanFilter.class, name = "BOOLEAN")})
@Data
public abstract class Filter {
     private String property;
     private Operands operand;



}
