package org.rhq.audit.producer;

import javax.jms.JMSException;
import javax.jms.Message;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.producer.ProducerConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces audit records.
 * 
 * The usage pattern is to create this object with a URL to the broker, then
 * {@link #sendAuditRecord(AuditRecord) send} audit records. When you are done
 * sending, call {@link #close()}.
 */
public class AuditRecordProducer extends AuditRecordProcessor {
    private final Logger log = LoggerFactory.getLogger(AuditRecordProducer.class);

    public AuditRecordProducer(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    /**
     * Send the given audit record to its destinations across the message bus.
     * Once sent, the audit record will get assigned a generated message ID.
     * That message ID will also be returned by this method.
     * 
     * If the audit record has an associated
     * {@link AuditRecord#getCorrelationId() correlation ID}, that ID will be
     * sent as part of the message.
     * 
     * @param auditRecord
     *            the record to send
     * @return the message ID
     * @throws JMSException
     */
    public MessageId sendAuditRecord(AuditRecord auditRecord) throws JMSException {
        // create the JMS message to be sent
        ProducerConnectionContext context = createProducerConnectionContext();
        Message msg = createMessage(context, auditRecord);

        // if the auditRecord is correlated with another, put the correlation ID in the Message to be sent
        if (auditRecord.getCorrelationId() != null) {
            msg.setJMSCorrelationID(auditRecord.getCorrelationId().toString());
        }

        if (auditRecord.getMessageId() != null) {
            log.debug("Non-null message ID [{}] will be ignored and a new one generated", auditRecord.getMessageId());
            auditRecord.setMessageId(null);
        }

        // now send the message to the broker
        context.getMessageProducer().send(msg);

        // put message ID into the auditRecord in case the caller wants to correlate it with another record
        MessageId messageId = new MessageId(msg.getJMSMessageID());
        auditRecord.setMessageId(messageId);

        return messageId;
    }

    protected ProducerConnectionContext createProducerConnectionContext() throws JMSException {
        return createProducerConnectionContext(getEndpoint());
    }
}
