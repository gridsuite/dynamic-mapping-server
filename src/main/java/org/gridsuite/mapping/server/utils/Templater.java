/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.utils;

import org.apache.commons.io.IOUtils;
import org.gridsuite.mapping.server.MappingConstants;
import org.gridsuite.mapping.server.dto.filters.AbstractFilter;
import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.gridsuite.mapping.server.service.implementation.ScriptServiceImpl;
import org.springframework.core.io.ClassPathResource;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public final class Templater {

    private Templater() {
        // Not Called
    }

    public static String flattenFilters(String composition, List<AbstractFilter> filters) {

        final String[] flattenedComposition = {composition};
        // WARNING: Will not work with multithreading
        filters.stream().forEach(filter -> {
            flattenedComposition[0] = flattenedComposition[0].replaceAll(filter.getFilterId() + "\\b", filter.convertFilterToString());
        });
        return flattenedComposition[0];
    }

    public static String mappingToScript(ScriptServiceImpl.SortedMapping sortedMapping) {
        String scriptTemplate;
        String sortedRulesTemplate;
        String ruleTemplate;
        try {
            scriptTemplate = IOUtils.toString(new ClassPathResource("script.st").getInputStream(), Charset.defaultCharset());
            sortedRulesTemplate = IOUtils.toString(new ClassPathResource("sortedRules.st").getInputStream(), Charset.defaultCharset());
            ruleTemplate = IOUtils.toString(new ClassPathResource("rule.st").getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load templates for groovy script generation !!");
        }

        ST script = new ST(scriptTemplate);
        ArrayList<String> imports = new ArrayList<>();
        String[] sortedRulesScripts = sortedMapping.getSortedRules().stream().map(sortedRules -> {
            // Preparing the imports
            imports.add(MappingConstants.IMPORT + sortedRules.getEquipmentClass());
            // Preparing the main template
            ST sortedRulesScript = new ST(sortedRulesTemplate);
            sortedRulesScript.add("equipmentClass", sortedRules.getEquipmentClass());
            sortedRulesScript.add("isGenerator", sortedRules.isGenerator());
            sortedRulesScript.add("collectionName", sortedRules.getCollectionName());
            String[] rulesScripts = sortedRules.getRules().stream().map(flatRule -> {
                ST ruleScript = new ST(ruleTemplate);
                ruleScript.add("composition", flatRule.getComposition());
                ruleScript.add("modelName", flatRule.getMappedModel().getModelName());
                ruleScript.add("parameterSetId", modelToParamSetId(flatRule.getMappedModel()));
                return ruleScript.render();
            }).toArray(String[]::new);
            sortedRulesScript.add("rulesScripts", rulesScripts);
            return sortedRulesScript.render();
        }).toArray(String[]::new);

        // Filling the  main template
        script.add("imports", imports.toArray());
        script.add("sortedRules", sortedRulesScripts);

        // TODO
        return script.render();
    }

    public static String equipmentTypeToCollectionName(EquipmentType equipmentType) {
        String equipmentCollection = "";
        switch (equipmentType) {
            case LOAD:
                equipmentCollection = "loads";
                break;
            case GENERATOR:
                equipmentCollection = "generators";
        }
        return equipmentCollection;
    }

    public static String equipmentTypeToClass(EquipmentType equipmentType) {
        String equipmentClass = "";
        switch (equipmentType) {
            case LOAD:
                equipmentClass = "Load";
                break;
            case GENERATOR:
                equipmentClass = "Generator";
        }
        return equipmentClass;
    }

    private static String modelToParamSetId(InstanceModelEntity instanceModelEntity) {
        String format = "\"%s\"";
        switch (instanceModelEntity.getParams().getType()) {
            case FIXED:
                break;
            case PREFIX:
                format = "\"%s\" + " + MappingConstants.EQUIPMENT_ID;
                break;
            case SUFFIX:
                format = MappingConstants.EQUIPMENT_ID + "+ \"%s\"";
                break;
        }
        return String.format(format, instanceModelEntity.getParams().getName());
    }
}
