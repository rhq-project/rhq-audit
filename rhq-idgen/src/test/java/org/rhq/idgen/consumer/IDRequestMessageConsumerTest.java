package org.rhq.idgen.consumer;

import java.util.HashMap;

import org.rhq.idgen.common.IDRequestMessage;
import org.testng.annotations.Test;

@Test
public class IDRequestMessageConsumerTest extends ProducerConsumerSetup {

    public void testLoggerConsumer() throws Exception {
        final HashMap<String, String> details = new HashMap<String, String>();
        details.put("onekey", "onevalue");
        details.put("twokey", "twovalue");

        IDRequestMessageConsumer listener = new IDRequestMessageConsumer();
        consumer.listen(listener);

        producer.sendIDRequestMessage(new IDRequestMessage("msg: no details"));
    }
}
