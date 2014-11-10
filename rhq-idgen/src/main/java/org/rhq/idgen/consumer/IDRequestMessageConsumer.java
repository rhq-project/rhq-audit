package org.rhq.idgen.consumer;

import java.util.Map;

import org.rhq.idgen.common.IDRequestMessage;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDRequestMessageConsumer extends BasicMessageListener<IDRequestMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(IDRequestMessage.class);

    @Override
    protected void onBasicMessage(IDRequestMessage basicMessage) {
        final String msg = basicMessage.getMessage();
        final Map<String, String> details = basicMessage.getDetails();

        LOG.debug("request for ID: msg=[{}], details=[{}]", msg, details);
    }
}
