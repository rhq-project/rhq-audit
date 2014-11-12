package org.rhq.audit.common;

import javax.jms.JMSException;

import org.rhq.msg.common.ConnectionContextFactory;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.MessageProcessor;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.producer.ProducerConnectionContext;

/**
 * Consumes and produces audit records.
 */
public class AuditRecordProcessor extends ConnectionContextFactory {

    private final MessageProcessor msgProcessor;

    public AuditRecordProcessor(String brokerURL) throws JMSException {
        super(brokerURL);
        msgProcessor = new MessageProcessor();
    }

    /**
     * Send the given audit record.
     * 
     * @param auditRecord
     *            the record to send
     * @return the message ID
     * @throws JMSException
     */
    public MessageId sendAuditRecord(AuditRecord auditRecord) throws JMSException {
        ProducerConnectionContext context = createProducerConnectionContext(getEndpoint());
        return msgProcessor.send(context, auditRecord);
    }

    /**
     * Listens for audit records.
     * 
     * @param listener
     *            the listener that processes the incoming audit records
     * @throws JMSException
     */
    public void listen(BasicMessageListener<AuditRecord> listener) throws JMSException {
        msgProcessor.listen(createConsumerConnectionContext(getEndpoint()), listener);
    }

    /**
     * @return the endpoint used for all audit messages
     */
    protected Endpoint getEndpoint() {
        return new Endpoint(Type.QUEUE, "audit");
    }
}
