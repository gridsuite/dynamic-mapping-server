package org.gridsuite.mapping.server.service.implementation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static java.util.stream.Collectors.groupingBy;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScriptServiceImpl implements ScriptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingServiceImpl.class);

    @Autowired
    ScriptServiceImpl self;

    private final InstanceModelRepository instanceModelRepository;
    private final MappingRepository mappingRepository;
    private final ScriptRepository scriptRepository;

    public ScriptServiceImpl(
            MappingRepository mappingRepository,
            ScriptRepository scriptRepository,
            InstanceModelRepository instanceModelRepository
    ) {
        this.instanceModelRepository = instanceModelRepository;
        this.mappingRepository = mappingRepository;
        this.scriptRepository = scriptRepository;
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
            scriptToSave.markNotNew();
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
    public Script saveScript(String scriptName, Script script) {
        scriptRepository.save(script.convertToEntity());
        return script;
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
            sortedRules = new ArrayList<>();
            Map<EquipmentType, List<Rule>> sortingRules = mapping.getRules().stream().collect(groupingBy(Rule::getEquipmentType));
            for (EquipmentType type : sortingRules.keySet()) {
                ArrayList<Rule> typedRules = new ArrayList<>(sortingRules.get(type));
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

        private boolean isGenerator;

        private String collectionName;

        private List<FlatRule> rules;

        public SortedRules(EquipmentType type, ArrayList<Rule> sortedRules) {
            String equipmentClass = Templater.equipmentTypeToClass(type);
            this.equipmentClass = equipmentClass;
            this.isGenerator = equipmentClass.equals("Generator");
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
                composition = Templater.flattenFilters(rule.getComposition(), rule.getFilters());
            } else {
                throw new MappingException(MODEL_NOT_FOUND);
            }

        }
    }
}