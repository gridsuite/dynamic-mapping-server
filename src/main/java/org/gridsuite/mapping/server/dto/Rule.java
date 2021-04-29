package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.*;
import org.gridsuite.mapping.server.dto.filters.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@ApiModel("Rule")
public class Rule   {
    @ApiModelProperty("Equipment type") private EquipmentType equipmentType;

    @ApiModelProperty("Mapped Model Instance ID")
    private String mappedModel;

    @ApiModelProperty("Composition")
    private String composition;

    @ApiModelProperty("Filters")
    private List<Filter> filters;

    public List<Filter> getFilters() {
        return filters;
    }

    public RuleEntity convertRuleToEntity(String mappingName) {
        UUID id =  UUID.randomUUID();
        return RuleEntity.builder().composition(composition).mappedModel(mappedModel).equipmentType(equipmentType).mappingName(mappingName).id(id).filters(filters.stream().map(filter -> filter.convertFilterToEntity(id)).collect(Collectors.toList())).build();
    }

    public Rule(RuleEntity ruleEntity) {
        equipmentType = ruleEntity.getEquipmentType();
        mappedModel = ruleEntity.getMappedModel();
        composition = ruleEntity.getComposition();
        filters = ruleEntity.getFilters().stream().map(filterEmbeddable -> Filter.createFilterFromEntity(filterEmbeddable)).collect(Collectors.toList());
    }



}

