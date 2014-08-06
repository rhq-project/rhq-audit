package org.rhq.audit.consumer;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;

/**
 * Consumes audit records.
 * 
 * The usage pattern is to create this object with a URL to the broker, then {@link #listen(BasicMessageListener)} for
 * audit records. When you are done listening, call {@link #close()}.
 */
public class AuditRecordConsumer extends AuditRecordProcessor {
    public AuditRecordConsumer(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    /**
     * Listens for audit records for the given subsystem.
     * 
     * @param subsystem
     *            identifies the types of audit records that are being listened
     *            for
     * @param listener
     *            the listener that processes the incoming audit records
     * @throws JMSException
     */
    public void listen(BasicMessageListener<AuditRecord> listener) throws JMSException {
        ConsumerConnectionContext context = createConsumerConnectionContext();
        MessageConsumer consumer = context.getMessageConsumer();
        consumer.setMessageListener(listener);
    }

    protected ConsumerConnectionContext createConsumerConnectionContext() throws JMSException {
        return createConsumerConnectionContext(getEndpoint());
    }
}
