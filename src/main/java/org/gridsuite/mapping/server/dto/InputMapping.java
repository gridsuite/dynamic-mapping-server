package org.gridsuite.mapping.server.dto;

import lombok.Data;
import org.gridsuite.mapping.server.model.MappingEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class InputMapping implements Mapping {
    private String name;
    private List<Rule> rules;

    public MappingEntity convertMappingToEntity() {
        MappingEntity convertedMapping = new MappingEntity();
        convertedMapping.setName(name);
        convertedMapping.setRules(rules.stream().map(rule -> rule.convertRuleToEntity(name)).collect(Collectors.toList()));
        return convertedMapping;
    }

    public InputMapping(MappingEntity mappingEntity) {
        name = mappingEntity.getName();
        rules = mappingEntity.getRules().stream().map(ruleEntity -> new Rule(ruleEntity)).collect(Collectors.toList());
    }


    // Needs to put the default rule last, hence going for the most specific rule to the most generic
    public Comparator<Rule> ruleComparator = Comparator.comparing(rule -> -rule.getFilters().size());
}
