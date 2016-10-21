package com.jpmorgan.ib.caonpd.cakeshop.conditions;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * @author I629630
 */
public class PostgresDataSourceConditon extends BaseCondition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (StringUtils.isBlank(databaseName)) {
            databaseName = context.getEnvironment().getProperty("cakeshop.database.vendor");
        }
        return  StringUtils.isNotBlank(databaseName) && databaseName.equalsIgnoreCase(POSTGRES);
    }

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.PARSE_CONFIGURATION;
    }
}
