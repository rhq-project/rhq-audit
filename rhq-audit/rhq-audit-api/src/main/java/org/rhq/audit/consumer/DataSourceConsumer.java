package org.rhq.audit.consumer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.consumer.BasicMessageListener;

/**
 * Stores audit records in the backend data store via the given DataSource.
 * 
 * @author John Mazzitelli
 */
public class DataSourceConsumer extends BasicMessageListener<AuditRecord> {
    private final DataSource dataSource;

    public DataSourceConsumer(DataSource ds) {
        if (ds == null) {
            throw new NullPointerException("datasource is null");
        }
        this.dataSource = ds;
    }

    @Override
    protected void onBasicMessage(AuditRecord basicMessage) {
        final MessageId id = basicMessage.getMessageId();
        final MessageId corId = basicMessage.getCorrelationId();
        final String msg = basicMessage.getMessage();
        final Map<String, String> details = basicMessage.getDetails();
        final Subsystem subsystem = basicMessage.getSubsystem();
        final long timestamp = basicMessage.getTimestamp();

        final String sql = String.format(
                "INSERT INTO RHQ_AUDIT (ID, CORRELATION_ID, SUBSYSTEM, TIMESTAMP, MESSAGE, DETAILS) VALUES ('%s', '%s', '%s', '%d', '%s', '%s')", id, corId,
                subsystem, timestamp, msg, details);

        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement stmt = conn.createStatement();
                int rowsAffected = stmt.executeUpdate(sql);
                if (rowsAffected != 1) {
                    throw new SQLException(String.format("%d rows were inserted!", rowsAffected));
                }
            } finally {
                conn.close();
            }
        } catch (SQLException e) {
            getLog().error("Audit record did not properly get inserted into datasource using sql [{}]", sql, e);
        }
    }
}
