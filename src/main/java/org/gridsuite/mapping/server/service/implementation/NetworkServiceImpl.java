/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.gridsuite.mapping.server.dto.EquipmentValues;
import org.gridsuite.mapping.server.dto.NetworkIdentification;
import org.gridsuite.mapping.server.dto.OutputNetwork;
import org.gridsuite.mapping.server.model.NetworkEntity;
import org.gridsuite.mapping.server.repository.NetworkRepository;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
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
            @Value("${backing-services.case.base-uri:http://case-server/}") String caseServerBaseUri,
            @Value("${backing-services.network-conversion.base-uri:http://network-conversion-server/}") String networkConversionServerBaseUri,
            NetworkRepository networkRepository) {
        this.caseServerBaseUri = caseServerBaseUri;
        this.networkConversionServerBaseUri = networkConversionServerBaseUri;
        this.networkRepository = networkRepository;
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

        HashMap<String, Set<String>> substationsPropertyValues = getSubstationsPropertyValues(network);
        HashMap<String, Set<String>> voltageLevelsPropertyValues = getVoltageLevelsPropertyValues(network);

        List<EquipmentValues> equipmentValuesList = new ArrayList<>();

        EquipmentValues generatorEquipmentValues = getGeneratorsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(generatorEquipmentValues);

        EquipmentValues loadsEquipmentValues = getLoadsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
        equipmentValuesList.add(loadsEquipmentValues);

        return equipmentValuesList;
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
            // Add future substations properties here
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
        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        UUID caseUuid = UUID.fromString(responseBody.substring(1, 37));
        NetworkIdentification networkIdentification = restTemplate.postForEntity(networkConversionServerBaseUri + "/" + NETWORK_CONVERSION_API_VERSION + "/networks?caseUuid=" + caseUuid, null, NetworkIdentification.class).getBody();
        if (networkIdentification == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }

        networkRepository.save(new NetworkEntity(networkIdentification.getNetworkUuid(), multipartFile.getOriginalFilename()));

        return getNetworkValuesFromExistingNetwork(networkIdentification.getNetworkUuid());
    }

    @Override
    public List<OutputNetwork> getNetworks() {
        return networkRepository.findAll().stream().map(networkEntity -> new OutputNetwork(networkEntity.getNetworkId(), networkEntity.getIidmName())).collect(Collectors.toList());
    }
}
