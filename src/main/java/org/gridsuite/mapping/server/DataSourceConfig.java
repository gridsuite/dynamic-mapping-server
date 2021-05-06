
package org.gridsuite.mapping.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:database.properties"})
//@PropertySource(value = {"file:/config/database.properties"}, ignoreResourceNotFound = true)
public class DataSourceConfig {
}
