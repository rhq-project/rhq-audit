package org.rhq.audit.common;

import javax.jms.JMSException;

import org.rhq.msg.common.AbstractMessageProcessor;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;

/**
 * Superclass that provides some functionality to process audit records. This can be the superclass to either a consumer
 * or producer.
 */
public abstract class AuditRecordProcessor extends AbstractMessageProcessor {

    public AuditRecordProcessor(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    /**
     * @return the endpoint used for all audit messages
     */
    protected Endpoint getEndpoint() {
        return new Endpoint(Type.QUEUE, "audit");
    }
}
