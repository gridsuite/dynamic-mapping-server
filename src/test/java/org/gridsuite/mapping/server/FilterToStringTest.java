/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.dto.filters.BooleanFilter;
import org.gridsuite.mapping.server.dto.filters.EnumFilter;
import org.gridsuite.mapping.server.dto.filters.NumberFilter;
import org.gridsuite.mapping.server.dto.filters.StringFilter;
import org.gridsuite.mapping.server.utils.Operands;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FilterToStringTest {

    @Test
    public void booleanFilterTest() {

        BooleanFilter filter = new BooleanFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        filter.setOperand(Operands.EQUALS);
        filter.setValue(true);

        // Test equals
        assertEquals("equipment.property == true", filter.convertFilterToString());

        // Test not equals
        filter.setOperand(Operands.NOT_EQUALS);
        assertEquals("equipment.property != true", filter.convertFilterToString());
    }

    @Test
    public void numberFilterTest() {

        NumberFilter filter = new NumberFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        ArrayList<Float> numberList = new ArrayList<>();
        numberList.add(1F);
        filter.setValue(numberList);

        filter.setOperand(Operands.EQUALS);

        // Test equals
        assertEquals("equipment.property == 1.000000", filter.convertFilterToString());

        // Test not equals
        filter.setOperand(Operands.NOT_EQUALS);
        assertEquals("equipment.property != 1.000000", filter.convertFilterToString());

        // Test Lower
        filter.setOperand(Operands.LOWER);
        assertEquals("equipment.property < 1.000000", filter.convertFilterToString());

        // Test Lower Or Equals
        filter.setOperand(Operands.LOWER_OR_EQUALS);
        assertEquals("equipment.property <= 1.000000", filter.convertFilterToString());

        // Test Higher
        filter.setOperand(Operands.HIGHER);
        assertEquals("equipment.property > 1.000000", filter.convertFilterToString());

        // Test Higher Or Equals
        filter.setOperand(Operands.HIGHER_OR_EQUALS);
        assertEquals("equipment.property >= 1.000000", filter.convertFilterToString());

        // Test multiple values operands
        numberList.add(2F);
        filter.setValue(numberList);
        String contains = "[1.0, 2.0].contains(equipment.property)";

        //Test in
        filter.setOperand(Operands.IN);
        assertEquals(contains, filter.convertFilterToString());

        // Test Not in
        filter.setOperand(Operands.NOT_IN);
        assertEquals("!" + contains, filter.convertFilterToString());

    }

    @Test
    public void stringFilterTest() {

        StringFilter filter = new StringFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        ArrayList<String> values = new ArrayList<>();
        values.add("value");
        filter.setValue(values);
        filter.setOperand(Operands.EQUALS);

        // Test equals
        String equality = "equipment.property.equals(\"value\")";
        assertEquals(equality, filter.convertFilterToString());

        // Test not equals
        filter.setOperand(Operands.NOT_EQUALS);
        assertEquals("!" + equality, filter.convertFilterToString());

        // Test Includes
        filter.setOperand(Operands.INCLUDES);
        assertEquals("equipment.property.contains(\"value\")", filter.convertFilterToString());

        //Test Starts with
        filter.setOperand(Operands.STARTS_WITH);
        assertEquals("equipment.property.startsWith(\"value\")", filter.convertFilterToString());

        //Test Ends with
        filter.setOperand(Operands.ENDS_WITH);
        assertEquals("equipment.property.endsWith(\"value\")", filter.convertFilterToString());

        // Test multiple values operands
        values.add("other");
        filter.setValue(values);
        String contains = "[\"value\", \"other\"].contains(equipment.property)";

        //Test in
        filter.setOperand(Operands.IN);
        assertEquals(contains, filter.convertFilterToString());

        // Test Not in
        filter.setOperand(Operands.NOT_IN);
        assertEquals("!" + contains, filter.convertFilterToString());

    }

    @Test
    public void enumFilterTest() {

        EnumFilter filter = new EnumFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        filter.setOperand(Operands.EQUALS);
        filter.setValue("value");

        // Test equals
        assertEquals("equipment.property.toString().equals(\"value\")", filter.convertFilterToString());

        // Test not equals
        filter.setOperand(Operands.NOT_EQUALS);
        assertEquals("!equipment.property.toString().equals(\"value\")", filter.convertFilterToString());
    }

}
