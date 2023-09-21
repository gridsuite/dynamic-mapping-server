/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.mapping.server.dto.*;
import org.gridsuite.mapping.server.dto.filters.AbstractFilter;
import org.gridsuite.mapping.server.model.NetworkEntity;
import org.gridsuite.mapping.server.repository.NetworkRepository;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.Methods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.gridsuite.mapping.server.MappingConstants.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
public class NetworkServiceImpl implements NetworkService {

    @Autowired
    private RestTemplate restTemplate;

    private final String caseServerBaseUri;
    private final String networkConversionServerBaseUri;

    @Autowired
    private NetworkStoreService networkStoreService;

    private final NetworkRepository networkRepository;

    @Autowired
    public NetworkServiceImpl(
            @Value("${powsybl.services.case-server.base-uri:http://case-server/}") String caseServerBaseUri,
            @Value("${powsybl.services.network-conversion-server.base-uri:http://network-conversion-server/}") String networkConversionServerBaseUri,
            NetworkRepository networkRepository) {
        this.caseServerBaseUri = caseServerBaseUri;
        this.networkConversionServerBaseUri = networkConversionServerBaseUri;
        this.networkRepository = networkRepository;
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

        HashMap<String, Set<String>> substationsPropertyValues = getSubstationsPropertyValues(network);
        HashMap<String, Set<String>> voltageLevelsPropertyValues = getVoltageLevelsPropertyValues(network);

        List<EquipmentValues> equipmentValuesList = new ArrayList<>();

        EquipmentValues generatorEquipmentValues = getGeneratorsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(generatorEquipmentValues);

        EquipmentValues loadsEquipmentValues = getLoadsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(loadsEquipmentValues);

        EquipmentValues busesEquipmentValues = getBusesEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(busesEquipmentValues);

        EquipmentValues linesEquipmentValues = getLinesEquipmentValues(network);
        equipmentValuesList.add(linesEquipmentValues);

        EquipmentValues twoWindingsTransformersEquipmentValues = getTwoWindingsTransformersEquipmentValues(network, substationsPropertyValues);
        equipmentValuesList.add(twoWindingsTransformersEquipmentValues);

        EquipmentValues shuntCompensatorsEquipmentValues = getShuntCompensatorsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(shuntCompensatorsEquipmentValues);

        EquipmentValues staticVarCompensatorsEquipmentValues = getStaticVarCompensatorEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(staticVarCompensatorsEquipmentValues);

        EquipmentValues hdvcLinesEquipmentValues = getHvdcLinesEquipmentValues(network);
        equipmentValuesList.add(hdvcLinesEquipmentValues);

        return new NetworkValues(networkUuid, equipmentValuesList);
    }

    private void setPropertyMap(HashMap<String, Set<String>> propertyMap, String value, String propertyName) {
        if (propertyMap.containsKey(propertyName)) {
            Set<String> propertyValues = propertyMap.get(propertyName);
            propertyValues.add(value);
        } else {
            Set<String> propertyValues = new HashSet<>();
            propertyValues.add(value);
            propertyMap.put(propertyName, propertyValues);
        }
    }

    private HashMap<String, Set<String>> getSubstationsPropertyValues(Network network) {
        final HashMap<String, Set<String>> substationsMap = new HashMap<>();
        network.getSubstations().forEach(substation -> {
            // Country
            Optional<Country> substationCountry = substation.getCountry();
            if (substationCountry.isPresent()) {
                String countryName = substationCountry.get().getName();
                setPropertyMap(substationsMap, countryName, COUNTRY_PROPERTY);
            }
            // Add future substations properties here

        });
        return substationsMap;
    }

    private HashMap<String, Set<String>> getVoltageLevelsPropertyValues(Network network) {
        HashMap<String, Set<String>> voltageLevelsMap = new HashMap<>();
        network.getVoltageLevels().forEach(voltageLevel -> {
            // nominalV
            String voltageLevelNominalV = String.valueOf(voltageLevel.getNominalV());
            setPropertyMap(voltageLevelsMap, voltageLevelNominalV, NOMINAL_V_PROPERTY);
            // Add future voltageLevels properties here
        });
        return voltageLevelsMap;
    }

    private EquipmentValues getGeneratorsEquipmentValues(Network network, HashMap<String, Set<String>> voltageLevelsPropertyValues, HashMap<String, Set<String>> substationsPropertyValues) {
        HashMap<String, Set<String>> generatorValuesMap = new HashMap<>();
        // Own properties
        network.getGenerators().forEach(generator -> {
            setPropertyMap(generatorValuesMap, String.valueOf(generator.getId()), ID_PROPERTY);
            setPropertyMap(generatorValuesMap, String.valueOf(generator.getEnergySource()), ENERGY_SOURCE_PROPERTY);
            setPropertyMap(generatorValuesMap, String.valueOf(generator.isVoltageRegulatorOn()), VOLTAGE_REGULATOR_ON_PROPERTY);
        });
        // Parent properties (merge unnecessary, no overlap in properties
        generatorValuesMap.putAll(voltageLevelsPropertyValues);
        generatorValuesMap.putAll(substationsPropertyValues);

        return new EquipmentValues(EquipmentType.GENERATOR, generatorValuesMap);
    }

    private EquipmentValues getLoadsEquipmentValues(Network network, HashMap<String, Set<String>> voltageLevelsPropertyValues, HashMap<String, Set<String>> substationsPropertyValues) {
        HashMap<String, Set<String>> loadValuesMap = new HashMap<>();
        // Own properties
        network.getLoads().forEach(load -> {
            setPropertyMap(loadValuesMap, String.valueOf(load.getLoadType()), LOAD_TYPE_PROPERTY);
            setPropertyMap(loadValuesMap, String.valueOf(load.getId()), ID_PROPERTY);
        });

        // Parent properties (merge unnecessary, no overlap in properties
        loadValuesMap.putAll(voltageLevelsPropertyValues);
        loadValuesMap.putAll(substationsPropertyValues);

        return new EquipmentValues(EquipmentType.LOAD, loadValuesMap);
    }

    private EquipmentValues getBusesEquipmentValues(Network network, HashMap<String, Set<String>> voltageLevelsPropertyValues, HashMap<String, Set<String>> substationsPropertyValues) {
        HashMap<String, Set<String>> busValuesMap = new HashMap<>();
        // Own properties
        network.getBusBreakerView().getBuses().forEach(bus -> setPropertyMap(busValuesMap, bus.getId(), ID_PROPERTY));

        // Parent properties, Bus is in a substation/voltageLevel
        busValuesMap.putAll(voltageLevelsPropertyValues);
        busValuesMap.putAll(substationsPropertyValues);

        return new EquipmentValues(EquipmentType.BUS, busValuesMap);
    }

    private EquipmentValues getLinesEquipmentValues(Network network) {
        HashMap<String, Set<String>> lineValuesMap = new HashMap<>();
        // Own properties
        network.getLines().forEach(line -> setPropertyMap(lineValuesMap, line.getId(), ID_PROPERTY));

        return new EquipmentValues(EquipmentType.LINE, lineValuesMap);
    }

    private EquipmentValues getTwoWindingsTransformersEquipmentValues(Network network, HashMap<String, Set<String>> substationsPropertyValues) {
        HashMap<String, Set<String>> twoWindingsTransformersValuesMap = new HashMap<>();
        // Own properties
        network.getTwoWindingsTransformers().forEach(twoWindingsTransformer -> setPropertyMap(twoWindingsTransformersValuesMap, twoWindingsTransformer.getId(), ID_PROPERTY));

        // Parent properties, Two Winding transformer is in a substation
        twoWindingsTransformersValuesMap.putAll(substationsPropertyValues);

        return new EquipmentValues(EquipmentType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformersValuesMap);
    }

    private EquipmentValues getShuntCompensatorsEquipmentValues(Network network, HashMap<String, Set<String>> voltageLevelsPropertyValues, HashMap<String, Set<String>> substationsPropertyValues) {
        HashMap<String, Set<String>> shuntCompensatorsValuesMap = new HashMap<>();
        // Own properties
        network.getShuntCompensators().forEach(shuntCompensator -> setPropertyMap(shuntCompensatorsValuesMap, shuntCompensator.getId(), ID_PROPERTY));

        // Parent properties, Shunt compensator is in a substation/voltageLevel
        shuntCompensatorsValuesMap.putAll(voltageLevelsPropertyValues);
        shuntCompensatorsValuesMap.putAll(substationsPropertyValues);

        return new EquipmentValues(EquipmentType.SHUNT_COMPENSATOR, shuntCompensatorsValuesMap);
    }

    private EquipmentValues getStaticVarCompensatorEquipmentValues(Network network, HashMap<String, Set<String>> voltageLevelsPropertyValues, HashMap<String, Set<String>> substationsPropertyValues) {
        HashMap<String, Set<String>> sVarValuesMap = new HashMap<>();
        // Own properties
        network.getStaticVarCompensators().forEach(svar -> setPropertyMap(sVarValuesMap, svar.getId(), ID_PROPERTY));

        // Parent properties, Static var compensator is in a substation/voltageLevel
        sVarValuesMap.putAll(voltageLevelsPropertyValues);
        sVarValuesMap.putAll(substationsPropertyValues);

        return new EquipmentValues(EquipmentType.STATIC_VAR_COMPENSATOR, sVarValuesMap);
    }

    private EquipmentValues getHvdcLinesEquipmentValues(Network network) {
        HashMap<String, Set<String>> hvdcLineValuesMap = new HashMap<>();
        // Own properties
        network.getHvdcLines().forEach(hvdcLine -> setPropertyMap(hvdcLineValuesMap, hvdcLine.getId(), ID_PROPERTY));

        return new EquipmentValues(EquipmentType.HVDC_LINE, hvdcLineValuesMap);
    }

    @Override
    public NetworkValues getNetworkValues(MultipartFile multipartFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        Resource fileResource = multipartFile.getResource();

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", fileResource);

        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename("network.iidm")
                .build();
        parts.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                caseServerBaseUri + "/" + CASE_API_VERSION + "/cases",
                HttpMethod.POST,
                requestEntity,
                // Cannot convert to UUID in test mocks
                String.class
        );
        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        UUID caseUuid = UUID.fromString(responseBody.substring(1, 37));
        String url = networkConversionServerBaseUri + "/" + NETWORK_CONVERSION_API_VERSION + "/networks?caseUuid=" + caseUuid + "&isAsyncRun=false";
        NetworkIdentification networkIdentification = restTemplate.postForEntity(url, Collections.emptyMap(), NetworkIdentification.class).getBody();
        if (networkIdentification == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        networkRepository.save(new NetworkEntity(networkIdentification.getNetworkUuid(), multipartFile.getOriginalFilename()));

        return getNetworkValuesFromExistingNetwork(networkIdentification.getNetworkUuid());
    }

    @Override
    public List<OutputNetwork> getNetworks() {
        return networkRepository.findAll().stream()
                .map(networkEntity -> new OutputNetwork(networkEntity.getNetworkId(), networkEntity.getNetworkName()))
                .collect(Collectors.toList());
    }

    private HashMap<String, HashMap<String, String>> getPropertyValuesBySubstations(Network network) {
        final HashMap<String, HashMap<String, String>> substationsMap = new HashMap<>();
        network.getSubstations().forEach(substation -> {
            final HashMap<String, String> substationMap = new HashMap<>();
            // Country
            Optional<Country> substationCountry = substation.getCountry();
            if (substationCountry.isPresent()) {
                String countryName = substationCountry.get().getName();
                substationMap.put(COUNTRY_PROPERTY, countryName);
            }
            // Add future substations properties here
            substationsMap.put(substation.getId(), substationMap);
        });
        return substationsMap;
    }

    private HashMap<String, HashMap<String, String>> getPropertyValuesByVoltageLevel(Network network) {
        final HashMap<String, HashMap<String, String>> voltageLevelsMap = new HashMap<>();
        network.getVoltageLevels().forEach(voltageLevel -> {
            final HashMap<String, String> voltageLevelMap = new HashMap<>();
            // nominalV
            String voltageLevelNominalV = String.valueOf(voltageLevel.getNominalV());
            voltageLevelMap.put(NOMINAL_V_PROPERTY, voltageLevelNominalV);

            // Add future voltageLevels properties here
            voltageLevelsMap.put(voltageLevel.getId(), voltageLevelMap);
        });
        return voltageLevelsMap;
    }

    private List<HashMap<String, String>> getPropertyValuesByGenerators(Network network, HashMap<String, HashMap<String, String>> voltageLevelsValues, HashMap<String, HashMap<String, String>> substationsValues) {

        List<HashMap<String, String>> generators = new ArrayList<>();
        network.getGenerators().forEach(generator -> {
            final HashMap<String, String> generatorMap = new HashMap<>();
            generatorMap.put(ID_PROPERTY, String.valueOf(generator.getId()));
            generatorMap.put(ENERGY_SOURCE_PROPERTY, String.valueOf(generator.getEnergySource()));
            generatorMap.put(VOLTAGE_REGULATOR_ON_PROPERTY, String.valueOf(generator.isVoltageRegulatorOn()));

            String voltageLevelId = generator.getTerminal().getVoltageLevel().getId();
            generatorMap.putAll(voltageLevelsValues.get(voltageLevelId));

            generator.getTerminal().getVoltageLevel().getSubstation().map(Substation::getId).ifPresent(subStationId -> {
                generatorMap.putAll(substationsValues.get(subStationId));
            });

            generators.add(generatorMap);
        });
        return generators;
    }

    private List<HashMap<String, String>> getPropertyValuesByLoads(Network network, HashMap<String, HashMap<String, String>> voltageLevelsValues, HashMap<String, HashMap<String, String>> substationsValues) {
        List<HashMap<String, String>> loads = new ArrayList<>();
        network.getLoads().forEach(load -> {
            final HashMap<String, String> loadMap = new HashMap<>();
            loadMap.put(ID_PROPERTY, String.valueOf(load.getId()));
            loadMap.put(LOAD_TYPE_PROPERTY, String.valueOf(load.getLoadType()));

            String voltageLevelId = load.getTerminal().getVoltageLevel().getId();
            loadMap.putAll(voltageLevelsValues.get(voltageLevelId));

            load.getTerminal().getVoltageLevel().getSubstation().map(Substation::getId).ifPresent(subStationId -> {
                loadMap.putAll(substationsValues.get(subStationId));
            });

            loads.add(loadMap);
        });
        return loads;
    }

    private List<HashMap<String, String>> getPropertyValuesByStaticVarCompensator(Network network, HashMap<String, HashMap<String, String>> voltageLevelsValues, HashMap<String, HashMap<String, String>> substationsValues) {
        List<HashMap<String, String>> svars = new ArrayList<>();
        network.getStaticVarCompensators().forEach(svar -> {
            final HashMap<String, String> svarMap = new HashMap<>();
            svarMap.put(ID_PROPERTY, String.valueOf(svar.getId()));

            String voltageLevelId = svar.getTerminal().getVoltageLevel().getId();
            svarMap.putAll(voltageLevelsValues.get(voltageLevelId));

            svar.getTerminal().getVoltageLevel().getSubstation().map(Substation::getId).ifPresent(subStationId -> {
                svarMap.putAll(substationsValues.get(subStationId));
            });

            svars.add(svarMap);
        });
        return svars;
    }

    private List<String> matchNetworkToRule(Network network, RuleToMatch rule) {
        HashMap<String, HashMap<String, String>> substationsValues = getPropertyValuesBySubstations(network);
        HashMap<String, HashMap<String, String>> voltageLevelsValues = getPropertyValuesByVoltageLevel(network);

        List<HashMap<String, String>> correspondingValues;
        switch (rule.getEquipmentType()) {
            case GENERATOR:
                correspondingValues = getPropertyValuesByGenerators(network, voltageLevelsValues, substationsValues);
                break;
            case LOAD:
                correspondingValues = getPropertyValuesByLoads(network, voltageLevelsValues, substationsValues);
                break;
            case STATIC_VAR_COMPENSATOR:
                correspondingValues = getPropertyValuesByStaticVarCompensator(network, voltageLevelsValues, substationsValues);
                break;
            default:
                throw new IllegalStateException();
        }

        return correspondingValues.stream()
                .map(equipment -> matchEquipmentToRule(equipment, rule))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String matchEquipmentToRule(HashMap<String, String> equipment, RuleToMatch rule) {
        AtomicReference<String> evaluatedComposition = new AtomicReference<>(rule.getComposition());
        rule.getFilters().stream().forEach(filter -> {
            boolean isFilterMatched = matchEquipmentToFilter(equipment, filter);
            evaluatedComposition.set(evaluatedComposition.get().replace(filter.getFilterId(), Methods.convertBooleanToString(isFilterMatched)));
        });
        boolean isMatched = false;
        try {
            ExpressionParser parser = new SpelExpressionParser();
            isMatched = (boolean) parser.parseExpression(evaluatedComposition.get()).getValue();

        } catch (SpelEvaluationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid composition", e);
        }

        return isMatched ? equipment.get(ID_PROPERTY) : null;
    }

    private boolean matchEquipmentToFilter(HashMap<String, String> equipment, AbstractFilter filter) {
        String valueToTest = equipment.get(filter.getProperty());
        return filter.matchValueToFilter(valueToTest);
    }
}
