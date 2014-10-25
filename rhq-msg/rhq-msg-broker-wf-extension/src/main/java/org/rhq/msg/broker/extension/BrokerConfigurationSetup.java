package org.rhq.msg.broker.extension;

import java.util.Map;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.logging.Logger;
import org.jboss.util.StringPropertyReplacer;

public class BrokerConfigurationSetup {

    private final Logger log = Logger.getLogger(BrokerConfigurationSetup.class);

    /**
     * The location of the configuration file.
     */
    private final String configurationFile;

    /**
     * Properties that will be used to complete the out-of-box configuration.
     */
    private final Map<String, String> customConfiguration;

    /**
     * Provides environment information about the server in which we are embedded.
     */
    private final ServerEnvironment serverEnvironment;

    public BrokerConfigurationSetup(String configFile, Map<String, String> customConfigProps, ServerEnvironment serverEnv) {
        if (configFile != null && !configFile.trim().isEmpty()) {
            this.configurationFile = configFile;
        } else {
            this.configurationFile = BrokerSubsystemExtension.BROKER_CONFIG_FILE_DEFAULT;
        }

        this.customConfiguration = customConfigProps;
        this.serverEnvironment = serverEnv;
        prepareConfiguration();
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public Map<String, String> getCustomConfiguration() {
        return customConfiguration;
    }

    public ServerEnvironment getServerEnvironment() {
        return serverEnvironment;
    }

    private void prepareConfiguration() {
        // perform some checking to setup defaults if need be
        Map<String, String> customConfigProps = this.customConfiguration;
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_NAME_SYSPROP, //
                BrokerSubsystemExtension.BROKER_NAME_DEFAULT);
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_PERSISTENT_SYSPROP, //
                Boolean.toString(BrokerSubsystemExtension.PERSISTENT_DEFAULT));
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_USE_JMX_SYSPROP, //
                Boolean.toString(BrokerSubsystemExtension.USE_JMX_DEFAULT));
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_CONNECTOR_NAME_SYSPROP, //
                BrokerSubsystemExtension.CONNECTOR_NAME_DEFAULT);
        prepareConfigurationProperty(customConfigProps, BrokerSubsystemExtension.BROKER_CONNECTOR_PROTOCOL_SYSPROP, //
                BrokerSubsystemExtension.CONNECTOR_PROTOCOL_DEFAULT);

        // replace ${x} tokens in all values
        for (Map.Entry<String, String> entry : customConfigProps.entrySet()) {
            String value = entry.getValue();
            if (value != null) {
                entry.setValue(StringPropertyReplacer.replaceProperties(value));
            }
        }
        return;
    }

    private void prepareConfigurationProperty(Map<String, String> customConfigProps, String prop, String defaultValue) {
        String propValue = customConfigProps.get(prop);
        if (propValue == null || propValue.trim().length() == 0 || "-".equals(propValue)) {
            log.debug("Broker configuration property [" + prop + "] was undefined; will default to [" + defaultValue + "]");
            customConfigProps.put(prop, defaultValue);
        }
        return;
    }
}