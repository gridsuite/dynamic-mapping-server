package org.gridsuite.mapping.server.utils;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return Methods.convertListToString(attribute == null ? List.of() : attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return Methods.convertStringToList(StringUtils.isBlank(dbData) ? "" : dbData);
    }
}

