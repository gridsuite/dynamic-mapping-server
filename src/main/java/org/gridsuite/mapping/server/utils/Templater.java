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
import org.gridsuite.mapping.server.model.ModelSetsGroupEntity;
import org.gridsuite.mapping.server.service.implementation.ScriptServiceImpl;
import org.springframework.core.io.ClassPathResource;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        filters.stream().forEach(filter ->
                flattenedComposition[0] = flattenedComposition[0].replaceAll(filter.getFilterId() + "\\b", filter.convertFilterToString())
        );
        return flattenedComposition[0];
    }

    public static String mappingToScript(ScriptServiceImpl.SortedMapping sortedMapping) {
        String scriptTemplate;
        String sortedRulesTemplate;
        String ruleTemplate;
        String automatonTemplate;
        String automatonPropertyTemplate;
        try {
            scriptTemplate = IOUtils.toString(new ClassPathResource("script.st").getInputStream(), Charset.defaultCharset());
            sortedRulesTemplate = IOUtils.toString(new ClassPathResource("sortedRules.st").getInputStream(), Charset.defaultCharset());
            ruleTemplate = IOUtils.toString(new ClassPathResource("rule.st").getInputStream(), Charset.defaultCharset());
            automatonTemplate = IOUtils.toString(new ClassPathResource("automaton.st").getInputStream(), Charset.defaultCharset());
            automatonPropertyTemplate = IOUtils.toString(new ClassPathResource("automatonProperty.st").getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load templates for groovy script generation !!");
        }

        ST script = new ST(scriptTemplate);
        Set<String> imports = new LinkedHashSet<>();
        // Rules
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

        // Automata
        String[] automataScripts = sortedMapping.getAutomata().stream().map(automaton -> {
            String familyModel = automaton.getExportedClassName();
            imports.add(MappingConstants.AUTOMATON_IMPORT);
            ST automatonScript = new ST(automatonTemplate);
            automatonScript.add("familyModel", familyModel);
            automatonScript.add("automatonId", automaton.getId());
            automatonScript.add("parameterSetId", automaton.getSetGroup());
            String[] propertiesScripts = automaton.convertToBasicProperties().stream().map(property -> {
                ST propertyScript = new ST(automatonPropertyTemplate);
                propertyScript.add("name", property.getName());
                propertyScript.add("value", property.getValue());
                return propertyScript.render();
            }).toArray(String[]::new);
            automatonScript.add("properties", propertiesScripts);
            return automatonScript.render();
        }).toArray(String[]::new);
        // Filling the  main template
        script.add("imports", imports.toArray());
        script.add("sortedRules", sortedRulesScripts);
        script.add("automata", automataScripts);
        script.add("addReturns", automataScripts.length > 0 && sortedRulesScripts.length > 0);
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

    private static String modelToParamSetId(ModelSetsGroupEntity modelSetsGroupEntity) {
        String format = "\"%s\"";
        switch (modelSetsGroupEntity.getType()) {
            case FIXED:
                break;
            case PREFIX:
                format = "\"%s\" + " + MappingConstants.EQUIPMENT_ID;
                break;
            case SUFFIX:
                format = MappingConstants.EQUIPMENT_ID + "+ \"%s\"";
                break;
        }
        return String.format(format, modelSetsGroupEntity.getName());
    }

    public static String setsToPar(List<ScriptServiceImpl.EnrichedParametersSet> sets) {
        String parFileTemplate;
        String parametersSetTemplate;
        String parameterTemplate;
        String refParameterTemplate;

        try {
            parFileTemplate = IOUtils.toString(new ClassPathResource("parFile.st").getInputStream(), Charset.defaultCharset());
            parametersSetTemplate = IOUtils.toString(new ClassPathResource("parametersSet.st").getInputStream(), Charset.defaultCharset());
            parameterTemplate = IOUtils.toString(new ClassPathResource("parameter.st").getInputStream(), Charset.defaultCharset());
            refParameterTemplate = IOUtils.toString(new ClassPathResource("refParameter.st").getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load templates for .par generation !!");
        }
        ST parFile = new ST(parFileTemplate, '{', '}');
        parFile.add("sets", sets.stream().map(set -> {
            ST setText = new ST(parametersSetTemplate, '{', '}');
            setText.add("id", set.getName());
            String[] parameterTexts = set.getParameters().stream().map(parameter -> {
                ST parameterText;
                if (parameter.getOrigin() == ParameterOrigin.NETWORK) {
                    parameterText = new ST(refParameterTemplate, '{', '}');
                    parameterText.add("origData", "IIDM");
                    parameterText.add("origName", parameter.getOriginName());
                } else {
                    parameterText = new ST(parameterTemplate, '{', '}');
                    parameterText.add("value", parameter.getValue());
                }
                parameterText.add("name", parameter.getName());
                parameterText.add("type", parameter.getType());
                return parameterText.render();
            }).toArray(String[]::new);
            setText.add("name", set.getName());
            setText.add("parameters", parameterTexts);
            return setText.render();
        }).toArray(String[]::new));
        return parFile.render();
    }
}
