/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public final class Methods {

    private Methods() {
        // not called
    }

    private static final String ESCAPED_COMMA = "##COMMA##";

    public static List<String> convertStringToList(String stringArray) {
        List<String> converted = new ArrayList<>();
        converted.addAll(Arrays.asList(stringArray.split(",")).stream().map(value -> value.trim().replace(ESCAPED_COMMA, ",")).collect(Collectors.toList()));
        return converted;
    }

    public static String convertListToString(List<String> array) {
        return array.stream().map(value -> value.replace(",", ESCAPED_COMMA)).collect(Collectors.joining(", "));
    }

    public static boolean convertStringToBoolean(String stringBool) {
        return stringBool.equals("true");
    }

    public static String convertBooleanToString(boolean bool) {
        return String.valueOf(bool);
    }

    public static List<Float> convertStringToNumberList(String stringNumberArray) {
        List<Float> converted = new ArrayList<>();
        converted.addAll(Arrays.asList(stringNumberArray.split(",")).stream().map(value -> Float.parseFloat(value.trim())).collect(Collectors.toList()));
        return converted;
    }

    public static String convertNumberListToString(List<Float> array) {
        return array.stream().map(value -> String.format(Locale.US, "%f", value)).collect(Collectors.joining(", "));
    }
}
