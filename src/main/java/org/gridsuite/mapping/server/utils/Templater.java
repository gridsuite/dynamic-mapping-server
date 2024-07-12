/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.utils;

import org.apache.commons.io.IOUtils;
import org.gridsuite.mapping.server.service.implementation.ParameterServiceImpl;
import org.springframework.core.io.ClassPathResource;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public final class Templater {

    private Templater() {
        // Not Called
    }

    public static String setsToPar(List<ParameterServiceImpl.EnrichedParametersSet> sets) {
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
                if (parameter.origin() == ParameterOrigin.NETWORK) {
                    parameterText = new ST(refParameterTemplate, '{', '}');
                    parameterText.add("origData", "IIDM");
                    parameterText.add("origName", parameter.originName());
                } else {
                    parameterText = new ST(parameterTemplate, '{', '}');
                    parameterText.add("value", parameter.value());
                }
                parameterText.add("name", parameter.name());
                parameterText.add("type", parameter.type());
                return parameterText.render();
            }).toArray(String[]::new);
            setText.add("name", set.getName());
            setText.add("parameters", parameterTexts);
            return setText.render();
        }).toArray(String[]::new));
        return parFile.render();
    }
}
