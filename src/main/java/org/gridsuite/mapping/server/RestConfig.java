/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Configuration
public class RestConfig {

    public static final String EXPORT_MAPPING_OBJECT_MAPPER_BEAN = "exportMappingObjectMapper";
    private static final Set<String> MAPPING_EXPORT_EXCLUDED_FIELDS = Set.of("id", "filterDirty", "modificationDate");

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.build();
    }

    @Bean(EXPORT_MAPPING_OBJECT_MAPPER_BEAN)
    public ObjectMapper exportMappingMapper(Jackson2ObjectMapperBuilder builder) {
        SimpleModule exclusionModule = new SimpleModule();
        exclusionModule.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                return beanProperties.stream()
                        .filter(prop -> !MAPPING_EXPORT_EXCLUDED_FIELDS.contains(prop.getName()))
                        .toList();
            }
        });
        return builder.build()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(exclusionModule);

    }
}
