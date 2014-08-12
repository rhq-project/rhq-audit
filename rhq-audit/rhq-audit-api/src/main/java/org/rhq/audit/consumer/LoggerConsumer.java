package org.rhq.audit.consumer;

import java.util.Map;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple audit record consumer that merely logs the audit record via a logger.
 * 
 * @author John Mazzitelli
 */
public class LoggerConsumer extends BasicMessageListener<AuditRecord> {
    @Override
    protected void onBasicMessage(AuditRecord basicMessage) {
        final MessageId id = basicMessage.getMessageId();
        final MessageId corId = basicMessage.getCorrelationId();
        final String msg = basicMessage.getMessage();
        final Map<String, String> details = basicMessage.getDetails();
        final Subsystem subsystem = basicMessage.getSubsystem();
        final long timestamp = basicMessage.getTimestamp();

        Logger logger = LoggerFactory.getLogger("AUDIT." + subsystem);
        logger.info("id=[{}], correlation=[{}], timestamp=[{}], subsystem=[{}], msg=[{}], details=[{}],", id, corId, timestamp, subsystem, msg, details);
    }
}
