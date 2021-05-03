package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.mapping.server.dto.filters.Filter;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.Templater;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class FlatRule {
    private EquipmentType equipmentType;

    private String mappedModel;

    private String composition;

    public FlatRule(Rule rule) {
        equipmentType = rule.getEquipmentType();
        mappedModel = rule.getMappedModel();
        composition = Templater.flattenFilters(rule.getComposition(), rule.getFilters() );
    }
}

