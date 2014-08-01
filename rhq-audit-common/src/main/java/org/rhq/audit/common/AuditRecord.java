package org.rhq.audit.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * Information about an audited event.
 * 
 * <ul>
 * <li>Message = a description of what happened; typically just a human readable
 * string.</li>
 * <li>Subsystem = identifies in which subsystem the event occurred. If not
 * specified, {@link Subsystem#MISCELLANEOUS} is used.</li>
 * <li>Timestamp = identified when the event occurred. If not specified, the
 * current time is used. Note this is not representative of when this
 * AuditRecord was sent to the backend store. This is supposed to identify when
 * the actual audited event happened.</li>
 * <li>Details = Additional name/value pairs of data that further provide
 * details on the audited event.</li>
 * </ul>
 * 
 * The {@link #getMessageId() message ID} is assigned by the messaging system
 * and so should not be explicitly set.
 * 
 * The {@link #getCorrelationId() correlation ID} is a message ID of another
 * AuditRecord that was sent previously. This is usually left unset unless this
 * AuditRecord needs to be correlated with another AuditRecord (as an example,
 * you can correlate a "Stopped" audit event with a "Stopping" event to record
 * when a process stopped and correlate that to when that process began to
 * stop).
 */
public class AuditRecord {
    // these are passed out-of-band of the message body - these attributes will
    // therefore not be JSON encoded
    private String messageId;
    private String correlationId;

    // these attributes make up the message body of the audit record, so these
    // will be exposed to the JSON output
    @Expose
    private final String message;
    @Expose
    private final Subsystem subsystem;
    @Expose
    private final Map<String, String> details;
    @Expose
    private final long timestamp;

    public static AuditRecord fromJSON(String json) {
        final Gson gson = createGsonBuilder();
        return gson.fromJson(json, AuditRecord.class);
    }

    public String toJSON() {
        final Gson gson = createGsonBuilder();
        return gson.toJson(this);
    }

    public AuditRecord(String message, Subsystem subsystem) {
        this(message, subsystem, null, 0);
    }

    public AuditRecord(String message, Subsystem subsystem, Map<String, String> details) {
        this(message, subsystem, details, 0);
    }

    public AuditRecord(String message, Subsystem subsystem, Map<String, String> details, long timestamp) {
        if (subsystem == null) {
            subsystem = Subsystem.MISCELLANEOUS;
        }
        if (timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }

        this.message = message;
        this.subsystem = subsystem;
        this.timestamp = timestamp;

        // make our own copy of the details data
        if (details != null && !details.isEmpty()) {
            this.details = new HashMap<String, String>(details);
        } else {
            this.details = null;
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getMessage() {
        return message;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public Map<String, String> getDetails() {
        if (details == null) {
            return null;
        }
        return Collections.unmodifiableMap(details);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("AuditRecord: [");
        str.append("message-id=");
        str.append(getMessageId());
        str.append(", correlation-id=");
        str.append(getCorrelationId());
        str.append(", json-body=[");
        str.append(toJSON());
        str.append("]]");
        return str.toString();
    }

    protected static Gson createGsonBuilder() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

}
