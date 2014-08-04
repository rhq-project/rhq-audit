package org.rhq.audit.common;

import javax.jms.JMSException;

import org.rhq.msg.common.AbstractMessageProcessor;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;

/**
 * Superclass that provides some functionality to process audit records.
 */
public abstract class AuditRecordProcessor extends AbstractMessageProcessor {

    public AuditRecordProcessor(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    /**
     * Given an audit record, this will return the messaging endpoint that would
     * be appropriate for that record.
     * 
     * @param auditRecord
     *            the record whose endpoint is to be returned
     * @return the endpoint
     */
    protected Endpoint getEndpointFromAuditRecord(AuditRecord auditRecord) {
        if (auditRecord == null) {
            throw new NullPointerException("auditRecord is null");
        }
        return getEndpointFromSubsystem(auditRecord.getSubsystem());
    }

    /**
     * Given a subsystem, this will return the messaging endpoint that would be
     * appropriate for that record. Messages for that subsystem will be sent and
     * received from the returned endpoint.
     * 
     * @param subsystem
     *            the subsystem whose endpoint is to be returned
     * @return the endpoint
     */
    protected Endpoint getEndpointFromSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            throw new NullPointerException("subsystem is null");
        }
        return new Endpoint(Type.QUEUE, subsystem.getName());
    }
}
