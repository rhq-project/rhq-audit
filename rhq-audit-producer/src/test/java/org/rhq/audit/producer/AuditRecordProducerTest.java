package org.rhq.audit.producer;

import org.junit.Assert;
import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.audit.common.test.AbstractEmbeddedBrokerWrapper;
import org.rhq.audit.common.test.VMEmbeddedBrokerWrapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AuditRecordProducerTest {

    private AuditRecordProducer producer;
    private AbstractEmbeddedBrokerWrapper broker;

    @BeforeMethod
    public void setupProducer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProducer(brokerURL);
    }

    @AfterMethod
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

    public void testMessageSend() throws Exception {
        AuditRecord auditRecord;
        auditRecord = new AuditRecord("test message", Subsystem.MISCELLANEOUS);
        Assert.assertNull(auditRecord.getMessageId());
        String messageId = producer.sendAuditRecord(auditRecord);
        Assert.assertNotNull(messageId);
        Assert.assertEquals(messageId, auditRecord.getMessageId());
    }
}
