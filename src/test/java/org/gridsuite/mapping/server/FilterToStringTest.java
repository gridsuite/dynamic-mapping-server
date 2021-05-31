package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.dto.filters.*;
import org.gridsuite.mapping.server.utils.Operands;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.junit.Assert.*;

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
    public void enumFilterTest() {

        EnumFilter filter = new EnumFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        ArrayList<String> values = new ArrayList<>();
        values.add("1");
        values.add("2");
        filter.setValue(values);

        filter.setOperand(Operands.EQUALS);

        // Test equals
        String equality = "equipment.property.equals(\"1\")";

        assertEquals(equality, filter.convertFilterToString());

        // Test not equals
        filter.setOperand(Operands.NOT_EQUALS);
        assertEquals("!" + equality, filter.convertFilterToString());

        // Test In
        String contains = "[\"1\", \"2\"].contains(equipment.property)";

        filter.setOperand(Operands.IN);
        assertEquals(contains, filter.convertFilterToString());

        //Test Not in

        filter.setOperand(Operands.NOT_IN);
        assertEquals("!" + contains, filter.convertFilterToString());
    }

    @Test
    public void numberFilterTest() {

        NumberFilter filter = new NumberFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        filter.setValue(1);

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
    }

    @Test
    public void stringFilterTest() {

        StringFilter filter = new StringFilter();

        filter.setFilterId("id");
        filter.setProperty("property");
        filter.setValue("value");
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
    }

}