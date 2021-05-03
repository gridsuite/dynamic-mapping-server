package org.gridsuite.mapping.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class methods {
    public static ArrayList<String> convertStringToList(String stringArray) {
        ArrayList<String> converted = new ArrayList();
         converted.addAll(Arrays.asList(stringArray.split("\\s*,\\s*")));
        return converted  ;
    }

    public static String convertListToString(List<String> array) {
        return  array.stream().reduce("", (acc, element) -> acc + " , " + element);
    }

    public static boolean convertStringToBoolean( String stringBool) {
        return stringBool == "true";
    }

    public static String convertBooleanToString( boolean bool) {
        return String.valueOf(bool);
    }

    public static float convertStringToNumber(String stringNumber) {
        return Float.parseFloat(stringNumber);
    }

    public static String convertNumberToString( float number) {
        return String.valueOf(number);
    }

}
