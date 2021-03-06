package org.rhq.audit.common;

import java.util.HashMap;
import java.util.Map;

import org.rhq.msg.common.MessageId;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class AuditRecordTest {

    // tests a minimal audit record with no details
    public void simpleConversion() {
        AuditRecord arec = new AuditRecord("my msg", new Subsystem("FOO"));
        String json = arec.toJSON();
        Assert.assertNotNull(json, "missing JSON");
        System.out.println("~~~~~~~~~~~~~~toString=" + arec);
        System.out.println("~~~~~~~~~~~~~~JSON=" + json);

        AuditRecord arec2 = AuditRecord.fromJSON(json);
        Assert.assertNotNull(arec2, "JSON conversion failed");
        Assert.assertNotSame(arec, arec2);
        Assert.assertEquals(arec.getMessage(), arec2.getMessage());
        Assert.assertEquals(arec.getSubsystem(), arec2.getSubsystem());
        Assert.assertEquals(arec.getTimestamp(), arec2.getTimestamp());
        Assert.assertEquals(arec.getDetails(), arec2.getDetails());
    }

    // test a full audit record with several details
    public void fullConversion() {
        Map<String,String> details = new HashMap<String,String>();
        details.put("key1", "val1");
        details.put("secondkey", "secondval");

        AuditRecord arec = new AuditRecord("my msg", new Subsystem("FOO"), details, 12345L);
        arec.setMessageId(new MessageId("12345"));
        arec.setCorrelationId(new MessageId("67890"));
        String json = arec.toJSON();
        System.out.println(json);
        Assert.assertNotNull(json, "missing JSON");

        AuditRecord arec2 = AuditRecord.fromJSON(json);
        Assert.assertNotNull(arec2, "JSON conversion failed");
        Assert.assertNotSame(arec, arec2);
        Assert.assertNull(arec2.getMessageId(), "Message ID should not be encoded in JSON");
        Assert.assertNull(arec2.getCorrelationId(), "Correlation ID should not be encoded in JSON");
        Assert.assertEquals(arec2.getMessage(), "my msg");
        Assert.assertEquals(arec2.getSubsystem().getName(), "FOO");
        Assert.assertEquals(arec2.getTimestamp(), 12345L);
        Assert.assertEquals(arec2.getDetails().size(), 2);
        Assert.assertEquals(arec2.getDetails().get("key1"), "val1");
        Assert.assertEquals(arec2.getDetails().get("secondkey"), "secondval");
        Assert.assertEquals(arec.getMessage(), arec2.getMessage());
        Assert.assertEquals(arec.getSubsystem(), arec2.getSubsystem());
        Assert.assertEquals(arec.getTimestamp(), arec2.getTimestamp());
        Assert.assertEquals(arec.getDetails(), arec2.getDetails());
    }
}
