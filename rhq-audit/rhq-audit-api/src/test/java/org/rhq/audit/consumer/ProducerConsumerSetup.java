package org.rhq.audit.consumer;

import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.msg.common.test.AbstractEmbeddedBrokerWrapper;
import org.rhq.msg.common.test.VMEmbeddedBrokerWrapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class ProducerConsumerSetup {

    private AbstractEmbeddedBrokerWrapper broker;
    protected AuditRecordProcessor producer;
    protected AuditRecordProcessor consumer;

    public ProducerConsumerSetup() {
        super();
    }

    @BeforeMethod
    public void setupProducerAndConsumer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProcessor(brokerURL);
        consumer = new AuditRecordProcessor(brokerURL);
    }

    @AfterMethod
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