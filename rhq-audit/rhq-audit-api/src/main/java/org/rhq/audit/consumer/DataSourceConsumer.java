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
    private final SqlGenerator sqlGenerator;

    /**
     * You can implement your own custom SQL generator and pass it to the constructor of DataSourceConsumer if you want
     * to insert the audit record in your own custom database.
     */
    public interface SqlGenerator {
        /**
         * Given an audit record, return the SQL that should be used to insert the record into the database.
         * 
         * @param auditRecord
         *            the audit record to be stored
         * @return the INSERT SQL statement that can be used to store the audit record
         */
        public String generateSql(AuditRecord auditRecord);
    }

    public DataSourceConsumer(DataSource ds) {
        this(ds, null);
    }

    /**
     * Creates the consumer that will store audit records to the given datasource using the INSERT SQL that is generated
     * by the given SQL generator object. If the SQL generator is null, a default SQL generator will be used that
     * assumes a table exists called RHQ_AUDIT with column names of ID, CORRELATION_ID, SUBSYSTEM, TIMESTAMP, MESSAGE,
     * and DETAILS.
     * 
     * @param ds
     *            the data source where the audit records are stored
     * @param sqlGenerator
     *            the object that generates the INSERT SQL statement used to insert the given audit record.
     */
    public DataSourceConsumer(DataSource ds, SqlGenerator sqlGenerator) {
        if (ds == null) {
            throw new NullPointerException("datasource is null");
        }
        this.dataSource = ds;

        // if no SQL generator given, use a default one that assumes a simple schema with one table called RHQ_AUDIT
        if (sqlGenerator == null) {
            sqlGenerator = new SqlGenerator() {
                @Override
                public String generateSql(AuditRecord auditRecord) {
                    final MessageId id = auditRecord.getMessageId();
                    final MessageId corId = auditRecord.getCorrelationId();
                    final String msg = auditRecord.getMessage();
                    final Map<String, String> details = auditRecord.getDetails();
                    final Subsystem subsystem = auditRecord.getSubsystem();
                    final long timestamp = auditRecord.getTimestamp();

                    final String sql = String.format(
                            "INSERT INTO RHQ_AUDIT (ID, CORRELATION_ID, SUBSYSTEM, TIMESTAMP, MESSAGE, DETAILS) VALUES ('%s', '%s', '%s', '%d', '%s', '%s')",
                            id, corId, subsystem, timestamp, msg, details);
                    return sql;
                }
            };
        }
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    protected void onBasicMessage(AuditRecord auditRecord) {

        final String sql = this.sqlGenerator.generateSql(auditRecord);

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
