package org.rhq.audit.producer;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.audit.common.Subsystem;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.test.AbstractEmbeddedBrokerWrapper;
import org.rhq.msg.common.test.VMEmbeddedBrokerWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AuditRecordProducerTest {

    private AuditRecordProcessor producer;
    private AbstractEmbeddedBrokerWrapper broker;

    @BeforeMethod
    public void setupProducer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProcessor(brokerURL);
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
        auditRecord = new AuditRecord("test audit record", Subsystem.MISCELLANEOUS);
        Assert.assertNull(auditRecord.getMessageId());
        MessageId messageId = producer.sendAuditRecord(auditRecord);
        Assert.assertNotNull(messageId);
        Assert.assertEquals(messageId, auditRecord.getMessageId());
    }
}
