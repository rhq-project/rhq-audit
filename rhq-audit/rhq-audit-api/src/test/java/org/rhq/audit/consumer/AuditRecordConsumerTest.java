package org.rhq.audit.consumer;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.audit.producer.AuditRecordProducer;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.test.AbstractEmbeddedBrokerWrapper;
import org.rhq.msg.common.test.StoreAndLatchBasicMessageListener;
import org.rhq.msg.common.test.VMEmbeddedBrokerWrapper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AuditRecordConsumerTest {

    private AbstractEmbeddedBrokerWrapper broker;
    private AuditRecordProducer producer;
    private AuditRecordConsumer consumer;

    @BeforeMethod
    public void setupProducerAndConsumer() throws Exception {
        broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProducer(brokerURL);
        consumer = new AuditRecordConsumer(brokerURL);
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

    public void testMessageSend() throws Exception {
        final int numberOfTestRecords = 5;
        final CountDownLatch latch = new CountDownLatch(numberOfTestRecords);
        final ArrayList<AuditRecord> records = new ArrayList<AuditRecord>();
        final StoreAndLatchBasicMessageListener<AuditRecord> listener = new StoreAndLatchBasicMessageListener<AuditRecord>(latch, records, null,
                AuditRecord.class);

        consumer.listen(listener);

        // send some audit records
        for (int i = 0; i < numberOfTestRecords; i++) {
            AuditRecord auditRecord = new AuditRecord("test audit message#" + i, Subsystem.MISCELLANEOUS);
            producer.sendAuditRecord(auditRecord);
        }
        latch.await(5, TimeUnit.SECONDS);

        // make sure the audit records flowed properly
        Assert.assertEquals(numberOfTestRecords, records.size());
        for (int i = 0; i < numberOfTestRecords; i++) {
            Assert.assertEquals(Subsystem.MISCELLANEOUS, records.get(i).getSubsystem());
            Assert.assertEquals("test audit message#" + i, records.get(i).getMessage());
            Assert.assertNull(records.get(i).getDetails());
            Assert.assertTrue(records.get(i).getTimestamp() > 0L);
            Assert.assertNotNull(records.get(i).getMessageId());
            Assert.assertNull(records.get(i).getCorrelationId());
        }
    }

    public void testCorrelatedMessages() throws Exception {
        final int numberOfTestRecords = 5;
        final CountDownLatch latch = new CountDownLatch(numberOfTestRecords);
        final ArrayList<AuditRecord> records = new ArrayList<AuditRecord>();
        final StoreAndLatchBasicMessageListener<AuditRecord> listener = new StoreAndLatchBasicMessageListener<AuditRecord>(latch, records, null,
                AuditRecord.class);

        consumer.listen(listener);

        // send some audit records, correlate the everything to the first one
        MessageId firstMessageId = null;
        for (int i = 0; i < numberOfTestRecords; i++) {
            AuditRecord auditRecord = new AuditRecord("test audit message#" + i, Subsystem.MISCELLANEOUS);
            if (firstMessageId != null) {
                auditRecord.setCorrelationId(firstMessageId);
                producer.sendAuditRecord(auditRecord);
            } else {
                firstMessageId = producer.sendAuditRecord(auditRecord);
            }
        }
        latch.await(5, TimeUnit.SECONDS);

        // make sure the audit records flowed properly
        firstMessageId = null;
        Assert.assertEquals(numberOfTestRecords, records.size());
        for (int i = 0; i < numberOfTestRecords; i++) {
            Assert.assertEquals(Subsystem.MISCELLANEOUS, records.get(i).getSubsystem());
            Assert.assertEquals("test audit message#" + i, records.get(i).getMessage());
            Assert.assertNull(records.get(i).getDetails());
            Assert.assertTrue(records.get(i).getTimestamp() > 0L);
            Assert.assertNotNull(records.get(i).getMessageId());
            if (i == 0) {
                Assert.assertNull(records.get(i).getCorrelationId());
                firstMessageId = records.get(i).getMessageId();
            } else {
                Assert.assertEquals(firstMessageId, records.get(i).getCorrelationId());
            }
        }
    }
}
