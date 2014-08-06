package org.rhq.audit.consumer;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.audit.common.Subsystem;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;

/**
 * Consumes audit records.
 * 
 * The usage pattern is to create this object with a URL to the broker, then
 * {@link #listen(Subsystem, BasicMessageListener)} for audit records. When you are done listening, call
 * {@link #close()}.
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
    public void listen(Subsystem subsystem, BasicMessageListener<AuditRecord> listener) throws JMSException {
        ConsumerConnectionContext context = createConsumerConnectionContext(subsystem);
        MessageConsumer consumer = context.getMessageConsumer();
        consumer.setMessageListener(listener);
    }

    /**
     * Creates a new connection context, reusing any existing connection that might have already been created. The
     * endpoint of the session is dictated by the given subsystem (each subsystem has its own endpoint).
     * 
     * @param subsystem
     * @return the context fully populated
     * @throws JMSException
     */
    protected ConsumerConnectionContext createConsumerConnectionContext(Subsystem subsystem) throws JMSException {
        return createConsumerConnectionContext(getEndpointFromSubsystem(subsystem));
    }
}
