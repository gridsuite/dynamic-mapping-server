package org.gridsuite.mapping.server.utils;

import java.util.Arrays;
import java.util.List;

public class methods {
    public static List<String> convertStringToList(String stringArray) {
        return  Arrays.asList(stringArray.split("\\s*,\\s*"));
    }

    public static String convertListToString(List<String> array) {
        return  array.stream().reduce("", (acc, element) -> acc + " , " + element);
    }


}
