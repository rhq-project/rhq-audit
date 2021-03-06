package org.rhq.audit.common;

import java.util.Map;

import org.rhq.msg.common.BasicMessage;

import com.google.gson.annotations.Expose;

/**
 * Information about an audited event.
 * 
 * A {@link #getSubsystem() subsystem} identifies in which subsystem the event occurred. If not specified,
 * {@link Subsystem#MISCELLANEOUS} is used.
 * 
 * The {@link #getTimestamp() timestamp} identifies when the event occurred. If not specified, the current time is used.
 * Note this is not representative of when this AuditRecord was sent to the backend store. This is supposed to identify
 * when the actual audited event happened.
 * 
 */
public class AuditRecord extends BasicMessage {
    @Expose
    private final Subsystem subsystem;
    @Expose
    private final long timestamp;

    public static AuditRecord fromJSON(String json) {
        return BasicMessage.fromJSON(json, AuditRecord.class);
    }

    public AuditRecord(String message, Subsystem subsystem) {
        this(message, subsystem, null, 0);
    }

    public AuditRecord(String message, Subsystem subsystem, Map<String, String> details) {
        this(message, subsystem, details, 0);
    }

    public AuditRecord(String message, Subsystem subsystem, long timestamp) {
        this(message, subsystem, null, timestamp);
    }

    public AuditRecord(String message, Subsystem subsystem, Map<String, String> details, long timestamp) {
        super(message, details);

        if (subsystem == null) {
            subsystem = Subsystem.MISCELLANEOUS;
        }
        if (timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }

        this.subsystem = subsystem;
        this.timestamp = timestamp;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
