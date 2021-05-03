package org.gridsuite.mapping.server.utils;

import org.gridsuite.mapping.server.dto.filters.Filter;
import org.stringtemplate.v4.ST;

import java.util.List;

public class Templater {

    public static String flattenFilters(String composition, List<Filter> filters) {
        final String[]  flattenedComposition = {composition};
        filters.stream().forEach(filter -> {
            flattenedComposition[0] = flattenedComposition[0].replaceAll(filter.getFilterId() + "\\b", filter.convertFilterToString());
        });
        return flattenedComposition[0];
    }
}
