/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public final class MappingConstants {

    private MappingConstants() {
        // Not Called
    }

    public static String EQUIPMENT_ID = "equipment.id";
    public static String IMPORT = "import com.powsybl.iidm.network.";
    public static String AUTOMATON_IMPORT = "import com.powsybl.dynawaltz.models.automatons.CurrentLimitAutomaton\nimport com.powsybl.iidm.network.Branch";
    public static String DEFAULT_MAPPING_NAME = "default";

    public static final String CASE_API_VERSION = "v1";
    public static final String NETWORK_CONVERSION_API_VERSION = "v1";

    public static final String ID_PROPERTY = "id";
    // Substations
    public static final String COUNTRY_PROPERTY = "terminal.voltageLevel.substation.country.name";
    // Voltage Levels
    public static final String NOMINAL_V_PROPERTY = "terminal.voltageLevel.nominalV";
    // Generators
    public static final String ENERGY_SOURCE_PROPERTY = "energySource";
    public static final String VOLTAGE_REGULATOR_ON_PROPERTY = "voltageRegulatorOn";
    // Loads
    public static final String LOAD_TYPE_PROPERTY = "loadType";

}
