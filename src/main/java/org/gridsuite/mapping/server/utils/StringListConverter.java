package org.gridsuite.mapping.server.utils;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute != null ? Methods.convertListToString(attribute) : null;
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return !StringUtils.isBlank(dbData) ? Methods.convertStringToList(dbData) : null;
    }
}

