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

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public final class Methods {

    private Methods() {
        // not called
    }

    public static ArrayList<String> convertStringToList(String stringArray) {
        ArrayList<String> converted = new ArrayList();
        converted.addAll(Arrays.asList(stringArray.split("\\s*,\\s*")));
        return converted;
    }

    public static String convertListToString(List<String> array) {
        return array.stream().reduce("", (acc, element) -> {
            if (acc.equals("")) {
                return element;
            } else {
                return acc + " , " + element;
            }
        });
    }

    public static boolean convertStringToBoolean(String stringBool) {
        return stringBool.equals("true");
    }

    public static String convertBooleanToString(boolean bool) {
        return String.valueOf(bool);
    }

    public static float convertStringToNumber(String stringNumber) {
        return Float.parseFloat(stringNumber);
    }

    public static String convertNumberToString(float number) {
        return String.valueOf(number);
    }

}
