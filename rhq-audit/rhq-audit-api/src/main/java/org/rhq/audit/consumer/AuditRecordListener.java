package org.rhq.audit.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.rhq.audit.common.AuditRecord;
import org.rhq.msg.common.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message listener that expects to receive a JSON-encoded AuditRecord.
 * Implementors need only implement the method that takes an AuditRecord; the
 * JSON decoding is handled for you.
 */
public abstract class AuditRecordListener implements MessageListener {
    protected final Logger log = LoggerFactory.getLogger(AuditRecordListener.class);

    @Override
    public void onMessage(Message message) {
        AuditRecord auditRecord;

        try {
            String receivedBody = ((TextMessage) message).getText();
            auditRecord = AuditRecord.fromJSON(receivedBody);

            // grab some headers and put them in the audit record
            auditRecord.setMessageId(new MessageId(message.getJMSMessageID()));
            if (message.getJMSCorrelationID() != null) {
                auditRecord.setCorrelationId(new MessageId(message.getJMSCorrelationID()));
            }

            log.trace("Received audit record: {}", auditRecord);
        } catch (JMSException e) {
            log.error("A message was received that was not a valid text message", e);
            return;
        } catch (Exception e) {
            log.error("A message was received that was not a valid JSON-encoded AuditRecord", e);
            return;
        }

        onAuditRecord(auditRecord);
    }

    protected abstract void onAuditRecord(AuditRecord auditRecord);

}
