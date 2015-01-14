package org.hawkular.audit.consumer;

import java.util.HashMap;

import org.hawkular.audit.common.AuditRecord;
import org.hawkular.audit.common.Subsystem;
import org.junit.Test;

public class LoggerConsumerTest extends ProducerConsumerSetup {

    @Test
    public void testLoggerConsumer() throws Exception {
        final long timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 24); // yesterday
        final HashMap<String, String> details = new HashMap<String, String>();
        details.put("onekey", "onevalue");
        details.put("twokey", "twovalue");
        
        LoggerConsumer listener = new LoggerConsumer();
        consumer.listen(listener);

        producer.sendAuditRecord(new AuditRecord("msg: no details, no timestamp", Subsystem.MISCELLANEOUS));
        producer.sendAuditRecord(new AuditRecord("msg: no details", new Subsystem("SUBSYSTEM.FOO"), timestamp));
        producer.sendAuditRecord(new AuditRecord("msg: no timestamp", new Subsystem("ANOTHER.SUBSYS"), details));
        producer.sendAuditRecord(new AuditRecord("full msg", new Subsystem("WOT.GORILLA"), details, timestamp));
    }
}
