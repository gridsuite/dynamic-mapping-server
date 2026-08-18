/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.filter.utils.FiltersUtils;
import org.gridsuite.mapping.server.dto.EquipmentValues;
import org.gridsuite.mapping.server.dto.MatchedRule;
import org.gridsuite.mapping.server.dto.NetworkValues;
import org.gridsuite.mapping.server.dto.RuleToMatch;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils.getFieldValue;
import static org.gridsuite.filter.utils.expertfilter.FieldType.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
public class NetworkServiceImpl implements NetworkService {

    private final NetworkStoreService networkStoreService;

    @Autowired
    public NetworkServiceImpl(NetworkStoreService networkStoreService) {
        this.networkStoreService = networkStoreService;
    }

    @Override
    public Network getNetwork(UUID networkUuid) {
        try {
            return networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
        } catch (PowsyblException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "network-store-client error", e);
        }
    }

    @Override
    public MatchedRule getNetworkMatches(UUID networkUuid, RuleToMatch ruleToMatch) {
        List<String> matches = matchNetworkToRule(getNetwork(networkUuid), ruleToMatch);
        return new MatchedRule(ruleToMatch.getRuleIndex(), matches);
    }

    @Override
    public NetworkValues getNetworkValuesFromExistingNetwork(UUID networkUuid) {
        Network network = getNetwork(networkUuid);

        List<EquipmentValues> equipmentValuesList = new ArrayList<>();

        EquipmentValues generatorEquipmentValues = getGeneratorsEquipmentValues(network);
        equipmentValuesList.add(generatorEquipmentValues);

        EquipmentValues loadsEquipmentValues = getLoadsEquipmentValues(network);
        equipmentValuesList.add(loadsEquipmentValues);

        EquipmentValues busesEquipmentValues = getBusesEquipmentValues(network);
        equipmentValuesList.add(busesEquipmentValues);

        EquipmentValues linesEquipmentValues = getLinesEquipmentValues(network);
        equipmentValuesList.add(linesEquipmentValues);

        EquipmentValues twoWindingsTransformersEquipmentValues = getTwoWindingsTransformersEquipmentValues(network);
        equipmentValuesList.add(twoWindingsTransformersEquipmentValues);

        EquipmentValues shuntCompensatorsEquipmentValues = getShuntCompensatorsEquipmentValues(network);
        equipmentValuesList.add(shuntCompensatorsEquipmentValues);

        EquipmentValues staticVarCompensatorsEquipmentValues = getStaticVarCompensatorEquipmentValues(network);
        equipmentValuesList.add(staticVarCompensatorsEquipmentValues);

        EquipmentValues hdvcLinesEquipmentValues = getHvdcLinesEquipmentValues(network);
        equipmentValuesList.add(hdvcLinesEquipmentValues);

        return new NetworkValues(equipmentValuesList);
    }

    private void setPropertyMap(HashMap<String, Set<String>> propertyMap, String value, String propertyName) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        if (propertyMap.containsKey(propertyName)) {
            Set<String> propertyValues = propertyMap.get(propertyName);
            propertyValues.add(value);
        } else {
            Set<String> propertyValues = new HashSet<>();
            propertyValues.add(value);
            propertyMap.put(propertyName, propertyValues);
        }
    }

    private void getSubstationsPropertyValues(HashMap<String, Set<String>> valuesMap, Identifiable<?> identifiable) {
        // Country
        setPropertyMap(valuesMap, getFieldValue(COUNTRY, "", identifiable), COUNTRY.name());
        // Add future substations properties here
    }

    private void getVoltageLevelsPropertyValues(HashMap<String, Set<String>> valuesMap, Identifiable<?> identifiable) {
        // nominalV
        setPropertyMap(valuesMap, getFieldValue(NOMINAL_VOLTAGE, "", identifiable), NOMINAL_VOLTAGE.name());
        // Add future voltageLevels properties here
    }

    private EquipmentValues getGeneratorsEquipmentValues(Network network) {
        HashMap<String, Set<String>> generatorValuesMap = new LinkedHashMap<>();

        network.getGeneratorStream().forEach(generator -> {
            // Own properties
            setPropertyMap(generatorValuesMap, getFieldValue(ID, "", generator), ID.name());
            setPropertyMap(generatorValuesMap, getFieldValue(ENERGY_SOURCE, "", generator), ENERGY_SOURCE.name());
            setPropertyMap(generatorValuesMap, getFieldValue(VOLTAGE_REGULATOR_ON, "", generator), VOLTAGE_REGULATOR_ON.name());
            // Up level properties
            getSubstationsPropertyValues(generatorValuesMap, generator);
            getVoltageLevelsPropertyValues(generatorValuesMap, generator);
        });

        return new EquipmentValues(EquipmentType.GENERATOR, generatorValuesMap);
    }

    private EquipmentValues getLoadsEquipmentValues(Network network) {
        HashMap<String, Set<String>> loadValuesMap = new LinkedHashMap<>();

        network.getLoads().forEach(load -> {
            // Own properties
            setPropertyMap(loadValuesMap, getFieldValue(ID, "", load), ID.name());
            setPropertyMap(loadValuesMap, getFieldValue(LOAD_TYPE, "", load), LOAD_TYPE.name());
            // Up level properties
            getSubstationsPropertyValues(loadValuesMap, load);
            getVoltageLevelsPropertyValues(loadValuesMap, load);
        });

        return new EquipmentValues(EquipmentType.LOAD, loadValuesMap);
    }

    private EquipmentValues getBusesEquipmentValues(Network network) {
        HashMap<String, Set<String>> busValuesMap = new LinkedHashMap<>();

        network.getBusBreakerView().getBusStream().forEach(bus -> {
            // Own properties
            setPropertyMap(busValuesMap, getFieldValue(ID, "", bus), ID.name());
            // Up level properties
            getSubstationsPropertyValues(busValuesMap, bus);
            getVoltageLevelsPropertyValues(busValuesMap, bus);
        });

        return new EquipmentValues(EquipmentType.BUS, busValuesMap);
    }

    private EquipmentValues getLinesEquipmentValues(Network network) {
        HashMap<String, Set<String>> lineValuesMap = new LinkedHashMap<>();

        // Own properties
        network.getLineStream().forEach(line -> setPropertyMap(lineValuesMap, getFieldValue(ID, "", line), ID.name()));

        return new EquipmentValues(EquipmentType.LINE, lineValuesMap);
    }

    private EquipmentValues getTwoWindingsTransformersEquipmentValues(Network network) {
        HashMap<String, Set<String>> twoWindingsTransformersValuesMap = new LinkedHashMap<>();

        network.getTwoWindingsTransformerStream().forEach(twoWindingsTransformer -> {
            // Own properties
            setPropertyMap(twoWindingsTransformersValuesMap, getFieldValue(ID, "", twoWindingsTransformer), ID.name());
            // Up level properties
            getSubstationsPropertyValues(twoWindingsTransformersValuesMap, twoWindingsTransformer);
        });

        return new EquipmentValues(EquipmentType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformersValuesMap);
    }

    private EquipmentValues getShuntCompensatorsEquipmentValues(Network network) {
        HashMap<String, Set<String>> shuntCompensatorsValuesMap = new LinkedHashMap<>();

        network.getShuntCompensatorStream().forEach(shuntCompensator -> {
            // Own properties
            setPropertyMap(shuntCompensatorsValuesMap, getFieldValue(ID, "", shuntCompensator), ID.name());
            // Up level properties
            getSubstationsPropertyValues(shuntCompensatorsValuesMap, shuntCompensator);
            getVoltageLevelsPropertyValues(shuntCompensatorsValuesMap, shuntCompensator);
        });

        return new EquipmentValues(EquipmentType.SHUNT_COMPENSATOR, shuntCompensatorsValuesMap);
    }

    private EquipmentValues getStaticVarCompensatorEquipmentValues(Network network) {
        HashMap<String, Set<String>> sVarValuesMap = new LinkedHashMap<>();

        network.getStaticVarCompensatorStream().forEach(svar -> {
            // Own properties
            setPropertyMap(sVarValuesMap, getFieldValue(ID, "", svar), ID.name());
            // Up level properties
            getSubstationsPropertyValues(sVarValuesMap, svar);
            getVoltageLevelsPropertyValues(sVarValuesMap, svar);
        });

        return new EquipmentValues(EquipmentType.STATIC_VAR_COMPENSATOR, sVarValuesMap);
    }

    private EquipmentValues getHvdcLinesEquipmentValues(Network network) {
        HashMap<String, Set<String>> hvdcLineValuesMap = new LinkedHashMap<>();
        // Own properties
        network.getHvdcLines().forEach(hvdcLine -> setPropertyMap(hvdcLineValuesMap, getFieldValue(ID, "", hvdcLine), ID.name()));

        return new EquipmentValues(EquipmentType.HVDC_LINE, hvdcLineValuesMap);
    }

    private List<String> matchNetworkToRule(Network network, RuleToMatch rule) {
        if (rule.getFilter() == null) {
            return Collections.emptyList();
        }
        List<Identifiable<?>> matchedEquipments = FiltersUtils.getIdentifiables(rule.getFilter(), network, null);

        // return only ids
        return matchedEquipments.stream().map(Identifiable::getId).toList();
    }

}
