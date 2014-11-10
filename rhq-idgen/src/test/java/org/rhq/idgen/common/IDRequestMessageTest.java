package org.rhq.idgen.common;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class IDRequestMessageTest {

    // tests a minimal audit record with no details
    public void simpleConversion() {
        IDRequestMessage req = new IDRequestMessage("my msg");
        String json = req.toJSON();
        Assert.assertNotNull(json, "missing JSON");
        System.out.println("~~~~~~~~~~~~~~toString=" + req);
        System.out.println("~~~~~~~~~~~~~~JSON=" + json);

        IDRequestMessage req2 = IDRequestMessage.fromJSON(json);
        Assert.assertNotNull(req2, "JSON conversion failed");
        Assert.assertNotSame(req, req2);
        Assert.assertEquals(req.getMessage(), req2.getMessage());
    }
}
