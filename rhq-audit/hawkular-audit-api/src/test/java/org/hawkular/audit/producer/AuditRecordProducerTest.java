package org.hawkular.audit.producer;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.AuditRecordProcessor;
import org.hawkular.audit.common.Subsystem;
import org.hawkular.bus.common.MessageId;
import org.hawkular.bus.common.test.AbstractEmbeddedBrokerWrapper;
import org.hawkular.bus.common.test.VMEmbeddedBrokerWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuditRecordProducerTest {

    private AuditRecordProcessor producer;
    private AbstractEmbeddedBrokerWrapper broker;

    @Before
    public void setupProducer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProcessor(brokerURL);
    }

    @After
    public void teardownProducer() throws Exception {
        if (producer != null) {
            producer.close();
            producer = null;
        }
        if (broker != null) {
            broker.stop();
            broker = null;
        }
    }

    @Test
    public void testMessageSend() throws Exception {
        AuditRecord auditRecord;
        auditRecord = new AuditRecord("test audit record", Subsystem.MISCELLANEOUS);
        Assert.assertNull(auditRecord.getMessageId());
        MessageId messageId = producer.sendAuditRecord(auditRecord);
        Assert.assertNotNull(messageId);
        Assert.assertEquals(messageId, auditRecord.getMessageId());
    }
}
