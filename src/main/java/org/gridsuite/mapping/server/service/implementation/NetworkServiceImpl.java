/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import com.powsybl.iidm.network.Country;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.mapping.server.dto.EquipmentValues;
import org.gridsuite.mapping.server.dto.NetworkIdentification;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.iidm.network.Network;

import java.util.*;

import static org.gridsuite.mapping.server.MappingConstants.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
public class NetworkServiceImpl implements NetworkService {

    @Autowired
    private RestTemplate restTemplate;

    private String caseServerBaseUri;
    private String networkConversionServerBaseUri;

    @Autowired
    private NetworkStoreService networkStoreService;

    @Autowired
    public NetworkServiceImpl(
            @Value("${backing-services.case.base-uri:http://case-server/}") String caseServerBaseUri,
            @Value("${backing-services.network-conversion.base-uri:http://network-conversion-server/}") String networkConversionServerBaseUri) {
        this.caseServerBaseUri = caseServerBaseUri;
        this.networkConversionServerBaseUri = networkConversionServerBaseUri;
    }

    private Network getNetwork(UUID networkUuid) {
        try {
            return networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Network '" + networkUuid + "' not found");
        }
    }

    @Override
    public List<EquipmentValues> getNetworkValuesFromExistingNetwork(UUID networkUuid) {
        Network network = getNetwork(networkUuid);

        HashMap<String, List<String>> substationsPropertyValues = getSubstationsPropertyValues(network);
        HashMap<String, List<String>> voltageLevelsPropertyValues = getVoltageLevelsPropertyValues(network);

        List<EquipmentValues> equipmentValuesList = new ArrayList<>();

        EquipmentValues generatorEquipmentValues = getGeneratorsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(generatorEquipmentValues);

        EquipmentValues loadsEquipmentValues = getLoadsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(loadsEquipmentValues);

        return equipmentValuesList;
    }

    private void setPropertyMap(HashMap<String, List<String>> propertyMap, String value, String propertyName) {
        if (propertyMap.containsKey(propertyName)) {
            List<String> propertyValues = propertyMap.get(propertyName);
            if (!propertyValues.contains(value)) {
                propertyValues.add(value);
            }
        } else {
            ArrayList<String> propertyValues = new ArrayList<>();
            propertyValues.add(value);
            propertyMap.put(propertyName, propertyValues);
        }
    }

    private HashMap<String, List<String>> getSubstationsPropertyValues(Network network) {
        final HashMap<String, List<String>> substationsMap = new HashMap<>();
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

    private HashMap<String, List<String>> getVoltageLevelsPropertyValues(Network network) {
        HashMap<String, List<String>> voltageLevelsMap = new HashMap<>();
        network.getVoltageLevels().forEach(voltageLevel -> {
            // nominalV
            String voltageLevelNominalV = String.valueOf(voltageLevel.getNominalV());
            setPropertyMap(voltageLevelsMap, voltageLevelNominalV, NOMINAL_V_PROPERTY);
            // Add future substations properties here
        });
        return voltageLevelsMap;
    }

    private EquipmentValues getGeneratorsEquipmentValues(Network network, HashMap<String, List<String>> voltageLevelsPropertyValues, HashMap<String, List<String>> substationsPropertyValues) {
        HashMap<String, List<String>> generatorValuesMap = new HashMap<>();
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

    private EquipmentValues getLoadsEquipmentValues(Network network, HashMap<String, List<String>> voltageLevelsPropertyValues, HashMap<String, List<String>> substationsPropertyValues) {
        HashMap<String, List<String>> loadValuesMap = new HashMap<>();
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

    @Override
    public List<EquipmentValues> getNetworkValues(MultipartFile multipartFile) {
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
                caseServerBaseUri + "/" + CASE_API_VERSION + "/cases/private",
                HttpMethod.POST,
                requestEntity,
                // Cannot convert to UUID in test mocks
                String.class
        );

        System.out.println(response.getBody());
        UUID caseUuid = UUID.fromString(response.getBody().substring(1, 37));
        NetworkIdentification networkIdentification = restTemplate.postForEntity(networkConversionServerBaseUri + "/" + NETWORK_CONVERSION_API_VERSION + "/networks?caseUuid=" + caseUuid, null, NetworkIdentification.class).getBody();
        return getNetworkValuesFromExistingNetwork(networkIdentification.getNetworkUuid());
    }
}
