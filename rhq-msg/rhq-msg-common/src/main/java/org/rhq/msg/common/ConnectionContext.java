package org.rhq.msg.common;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Session;

/**
 * This is a simple POJO that just contains objects related to particular connection. This object does not distinguish
 * between a producer's connection or consumer's connection - that is the job of the subclasses.
 */
public class ConnectionContext {
    private Connection connection;
    private Session session;
    private Destination destination;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
