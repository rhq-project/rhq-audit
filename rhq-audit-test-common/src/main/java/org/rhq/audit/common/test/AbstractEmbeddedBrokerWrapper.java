package org.rhq.audit.common.test;

import org.rhq.audit.broker.EmbeddedBroker;

public abstract class AbstractEmbeddedBrokerWrapper {
    private EmbeddedBroker broker;

    public void setBroker(EmbeddedBroker b) {
        if (b == null) {
            throw new NullPointerException("broker must not be null");
        }
        broker = b;
    }

    public EmbeddedBroker getBroker() {
        return broker;
    }

    public void start() throws Exception {
        getBroker().startBroker();
    }

    public void stop() throws Exception {
        getBroker().stopBroker();
    }

    public abstract String getBrokerURL();
}
