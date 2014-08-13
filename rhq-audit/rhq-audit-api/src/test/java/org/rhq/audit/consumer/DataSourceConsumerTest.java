package org.rhq.audit.consumer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class DataSourceConsumerTest extends ProducerConsumerSetup {

    public void testDataSourceConsumer() throws Exception {
        final long timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 24); // yesterday
        final HashMap<String, String> details = new HashMap<String, String>();
        details.put("onekey", "onevalue");
        details.put("twokey", "twovalue");
        final CountDownLatch latch = new CountDownLatch(4); // counts down to count our four test audit records

        DataSource mockDs = Mockito.mock(DataSource.class);
        Connection mockConn = Mockito.mock(Connection.class);
        Statement mockStmt = Mockito.mock(Statement.class);
        Mockito.when(mockDs.getConnection()).thenReturn(mockConn);
        Mockito.when(mockConn.createStatement()).thenReturn(mockStmt);
        Mockito.when(mockStmt.executeUpdate(Mockito.anyString())).thenAnswer(new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                String sql = (String) invocation.getArguments()[0];
                if (sql.contains("INSERT")) {
                    // if we get here, we know the DataSourceConsumer is trying to INSERT the data to the database
                    latch.countDown();
                    return 1; // simulate that we inserted one row in the database
                } else {
                    throw new SQLException("invalid sql: " + sql);
                }
            }
        });

        DataSourceConsumer listener = new DataSourceConsumer(mockDs);
        consumer.listen(listener);

        producer.sendAuditRecord(new AuditRecord("msg: no details, no timestamp", Subsystem.MISCELLANEOUS));
        producer.sendAuditRecord(new AuditRecord("msg: no details", new Subsystem("SUBSYSTEM.FOO"), timestamp));
        producer.sendAuditRecord(new AuditRecord("msg: no timestamp", new Subsystem("ANOTHER.SUBSYS"), details));
        producer.sendAuditRecord(new AuditRecord("full msg", new Subsystem("WOT.GORILLA"), details, timestamp));

        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS), "Looks like we missed some events");
    }
}
