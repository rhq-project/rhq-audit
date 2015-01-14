package org.hawkular.audit.consumer;

import org.hawkular.audit.common.AuditRecordProcessor;
import org.hawkular.bus.common.test.AbstractEmbeddedBrokerWrapper;
import org.hawkular.bus.common.test.VMEmbeddedBrokerWrapper;
import org.junit.After;
import org.junit.Before;

public class ProducerConsumerSetup {

    private AbstractEmbeddedBrokerWrapper broker;
    protected AuditRecordProcessor producer;
    protected AuditRecordProcessor consumer;

    public ProducerConsumerSetup() {
        super();
    }

    @Before
    public void setupProducerAndConsumer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProcessor(brokerURL);
        consumer = new AuditRecordProcessor(brokerURL);
    }

    @After
    public void teardownProducerAndConsumer() throws Exception {
        if (producer != null) {
            producer.close();
            producer = null;
        }
        if (consumer != null) {
            consumer.close();
            consumer = null;
        }
        if (broker != null) {
            broker.stop();
            broker = null;
        }
    }

}