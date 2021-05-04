package org.gridsuite.mapping.server.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import static org.gridsuite.mapping.server.MappingException.Type.*;
import org.gridsuite.mapping.server.MappingException;

import org.gridsuite.mapping.server.dto.*;
import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.ScriptEntity;
import org.gridsuite.mapping.server.repository.InstanceModelRepository;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.service.ScriptService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.Templater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScriptServiceImpl implements ScriptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingServiceImpl.class);

    @Autowired
    ScriptServiceImpl self;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final InstanceModelRepository instanceModelRepository;
    private final MappingRepository mappingRepository;
    private final ScriptRepository scriptRepository;

    public ScriptServiceImpl(
            MappingRepository mappingRepository,
            ScriptRepository scriptRepository,
            InstanceModelRepository instanceModelRepository,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.instanceModelRepository = instanceModelRepository;
        this.mappingRepository = mappingRepository;
        this.scriptRepository = scriptRepository;
        this.webClient =  webClientBuilder.build();
        this.objectMapper = objectMapper;
    }
    @Override
    public Script createFromMapping(String mappingName) {
        Optional<MappingEntity> foundMapping = mappingRepository.findByName(mappingName);
        if (foundMapping.isPresent()) {
            SortedMapping sortedMapping = new SortedMapping(new InputMapping(foundMapping.get()));
            String createdScript = Templater.mappingToScript(sortedMapping);
            // TODO: Add Date or randomise to ensure uniqueness
            String savedScriptName = sortedMapping.getName() + "-script";
            ScriptEntity scriptToSave = new ScriptEntity(savedScriptName, sortedMapping.getName(), createdScript);
            scriptRepository.save(scriptToSave);
            return new Script(scriptToSave);
        } else {
            throw new MappingException(MAPPING_NOT_FOUND);
        }
    }

    @Override
    public List<Script> getAllScripts() {
        return scriptRepository.findAll().stream().map(scriptEntity -> new Script(scriptEntity)).collect(Collectors.toList());
    }

    @Override
    public Void updateScript(String scriptName, Script script) {
        return null;
    }

    @Override
    public Void deleteScript(String scriptName) {
        return null;
    }



    @Getter
    @Setter
    public class SortedMapping implements Mapping {
        private String name;
        private ArrayList<SortedRules> sortedRules;

        public SortedMapping(InputMapping mapping) {
            name = mapping.getName();
            sortedRules = new ArrayList<SortedRules>();
            HashMap<EquipmentType, ArrayList<Rule>> sortingRules =  new HashMap<EquipmentType, ArrayList<Rule>>();
            mapping.getRules().stream().forEach(rule -> {
                EquipmentType ruleType = rule.getEquipmentType();
                if (sortingRules.keySet().contains(ruleType)) {
                    ArrayList<Rule> associatedRules = sortingRules.get(ruleType);
                    associatedRules.add(rule);
                } else {
                    ArrayList<Rule> newRules = new ArrayList<Rule>();
                    newRules.add(rule);
                    sortingRules.put(ruleType, newRules);
                }
            });

            for (EquipmentType type: sortingRules.keySet()) {
                ArrayList<Rule> typedRules =  sortingRules.get(type);
                typedRules.sort(Rule.ruleComparator);
                sortedRules.add(new SortedRules(type, typedRules));
            }
        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class SortedRules {

        private String equipmentClass;

        private String collectionName;

        private List<FlatRule> rules;

        public SortedRules(EquipmentType type, ArrayList<Rule> sortedRules) {
            this.equipmentClass = Templater.equipmentTypeToClass(type);
            this.collectionName = Templater.equipmentTypeToCollectionName(type);
            rules = sortedRules.stream().map(rule -> new FlatRule(rule)).collect(Collectors.toList());
        }
    }

    @Getter
    @AllArgsConstructor
    public class FlatRule {
        private EquipmentType equipmentType;

        private InstanceModelEntity mappedModel;

        private String composition;

        public FlatRule(Rule rule) {
            Optional<InstanceModelEntity> foundModel = instanceModelRepository.findById(rule.getMappedModel());
            if (foundModel.isPresent()) {
                equipmentType = rule.getEquipmentType();
                mappedModel = foundModel.get();
                composition = Templater.flattenFilters(rule.getComposition(), rule.getFilters() );
            } else {
                throw new MappingException(MODEL_NOT_FOUND);
            }

        }
    }
}
