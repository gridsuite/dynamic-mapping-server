package org.gridsuite.mapping.server.service.implementation;

import com.powsybl.iidm.network.Country;
import com.powsybl.network.store.client.PreloadingStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.EquipmentValues;
import org.gridsuite.mapping.server.service.NetworkService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    private WebClient webClient;

    private String caseServerBaseUri;
    private String networkConversionServerBaseUri;

    @Autowired
    private NetworkStoreService networkStoreService;

    @Autowired
    public NetworkServiceImpl(
            @Value("${backing-services.case.base-uri:http://case-server/}") String caseServerBaseUri,
            @Value("${backing-services.network-conversion.base-uri:http://network-conversion-server/}") String networkConversionServerBaseUri,
            WebClient.Builder webClientBuilder) {
        this.caseServerBaseUri = caseServerBaseUri;
        this.networkConversionServerBaseUri = networkConversionServerBaseUri;

        this.webClient = webClientBuilder.build();
    }

//    private Flux<NetworkElement> getNetworkElements(Mono<NetworkIdentification> network, String elementType) {
//        return network.flatMapMany(networkId -> {
//            Flux<NetworkElement> networkGenerators = webClient.get()
//                    .uri(networkStoreServerBaseUri + "/" + networkId.networkUuid + "/" + elementType)
//                    .retrieve()
//                    .bodyToFlux(NetworkElement.class)
//                    .flatMap(networkElement -> {
//                        networkElement.setType(elementType);
//                        return networkElement;
//                    });
//        });
//    }

    private Network getNetwork(UUID networkUuid) {
        try {
            return networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Network '" + networkUuid + "' not found");
        }
    }

    @Override
    public Flux<EquipmentValues> getNetworkValuesFromExistingCase(Mono<UUID> caseUUID) {
        Mono<NetworkIdentification> networkIdentification = webClient.post()
                .uri(networkConversionServerBaseUri + "/" + NETWORK_CONVERSION_API_VERSION + "/networks")
                .retrieve()
                .bodyToMono(NetworkIdentification.class);

        return networkIdentification.flatMapMany(networkIdentification1 -> {
            Network network = getNetwork(UUID.fromString(networkIdentification1.networkUuid));

            HashMap<String, List<String>> substationsPropertyValues = getSubstationsPropertyValues(network);
            HashMap<String, List<String>> voltageLevelsPropertyValues = getVoltageLevelsPropertyValues(network);

            EquipmentValues generatorEquipmentValues = getGeneratorsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);
            EquipmentValues loadsEquipmentValues = getLoadsEquipmentValues(network, voltageLevelsPropertyValues, substationsPropertyValues);

            return Flux.just(generatorEquipmentValues, loadsEquipmentValues);

        });
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
    public Flux<EquipmentValues> getNetworkValues(Mono<FilePart> multipartFile) {
        Mono<UUID> caseUUID = multipartFile.flatMap(file -> {
            MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
            multipartBodyBuilder.part("file", file);

            return webClient.post()
                    .uri(caseServerBaseUri + "/" + CASE_API_VERSION + "/cases/private")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA.toString())
                    .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus != HttpStatus.OK, clientResponse -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "An error occured while importing the file")))
                    .bodyToMono(UUID.class);
        });

        return getNetworkValuesFromExistingCase(caseUUID);
    }

    @Getter
    @AllArgsConstructor
    public class NetworkIdentification {
        private String networkId;
        private String networkUuid;
    }

    @Getter
    @Setter
    public class NetworkElement {
        private String type;
        private String id;
        private Map<String, String> attributes;
    }
}
