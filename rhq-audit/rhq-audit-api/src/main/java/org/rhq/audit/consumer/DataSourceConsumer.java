package org.rhq.audit.consumer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.msg.common.MessageId;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores audit records in the backend data store via the given DataSource.
 * 
 * @author John Mazzitelli
 */
public class DataSourceConsumer extends BasicMessageListener<AuditRecord> {
    private final Logger log = LoggerFactory.getLogger(DataSourceConsumer.class);

    private DataSource dataSource;
    private SqlGenerator sqlGenerator;

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

        /**
         * Asks the SQL generator to initialize its schema if required.
         */
        public void createSchema(DataSource ds);
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }

    protected void setDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("datasource is null");
        }
        this.dataSource = dataSource;
    }

    protected SqlGenerator getSqlGenerator() {
        return this.sqlGenerator;
    }

    protected void setSqlGenerator(SqlGenerator sqlGenerator) {
        // if no SQL generator given, use a default one that assumes a simple schema with one table
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

                    final String sql = String.format("INSERT INTO RHQ_AUDIT " + //
                            "(ID, CORRELATION_ID, SUBSYSTEM, AUDIT_TIME, MESSAGE, DETAILS) " + //
                            "VALUES ('%s', '%s', '%s', %d, '%s', '%s')", //
                            id, corId, subsystem, timestamp, msg, details);
                    return sql;
                }

                @Override
                public void createSchema(DataSource ds) {
                    String createString = "CREATE TABLE RHQ_AUDIT (" + //
                            "ID VARCHAR(512) NULL," + //
                            "CORRELATION_ID VARCHAR(512) NULL," + //
                            "SUBSYSTEM VARCHAR(512) NULL," + //
                            "AUDIT_TIME LONG NULL," + //
                            "MESSAGE VARCHAR(4096) NULL," + //
                            "DETAILS VARCHAR(4096) NULL)";

                    Connection conn = null;
                    try {
                        conn = ds.getConnection();
                        DatabaseMetaData metadata = conn.getMetaData();
                        ResultSet tables = metadata.getTables(null, null, "RHQ_AUDIT", new String[] { "TABLE" });
                        if (!tables.next()) {
                            Statement stmt = conn.createStatement();
                            stmt.executeUpdate(createString);
                            log.info("Audit schema has been created");
                        } else {
                            log.info("Audit schema exists.");
                        }
                    } catch (SQLException sqle) {
                        log.error("Failed to create audit schema - audit subsystem is most likely in a bad state", sqle);
                    } finally {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException sqle) {
                                log.error("Failed to close connection", sqle);
                            }
                        }
                    }
                }
            };
        }

        this.sqlGenerator = sqlGenerator;
    }

    /**
     * Call this to initialize the consumer which verifies it is ready and will attempt to create a schema.
     *
     * @param ds
     *            the datasource
     * @param sg
     *            the sql generator to use - if <code>null</code>, one is created that uses the default schema
     */
    protected void initialize(DataSource ds, SqlGenerator sg) {
        setDataSource(ds);
        setSqlGenerator(sg);
        getSqlGenerator().createSchema(getDataSource());
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
