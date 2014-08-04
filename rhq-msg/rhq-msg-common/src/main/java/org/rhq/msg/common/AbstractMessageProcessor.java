package org.rhq.msg.common;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass that provides some functionality to connect and process messages.
 */
public abstract class AbstractMessageProcessor {

    private final Logger log = LoggerFactory.getLogger(AbstractMessageProcessor.class);
    protected final ConnectionFactory connectionFactory;
    private Connection connection;

    public AbstractMessageProcessor(String brokerURL) throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        log.debug("{} has been created: {}", this.getClass().getSimpleName(), brokerURL);
    }

    /**
     * This method should be called when this processor is no longer needed.
     * This will free up resources and close any open connection.
     * 
     * @throws JMSException
     */
    public void close() throws JMSException {
        Connection conn = getConnection();
        if (conn != null) {
            conn.close();
        }
        log.debug("{} has been closed", this);
    }

    protected ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * The stored connection.
     * 
     * NOTE: This is not necessarily the connection created via calling
     * {@link #createConnection(ConnectionContext)}.
     * 
     * @param connection
     * 
     * @see #createConnection(ConnectionContext)
     */
    protected Connection getConnection() {
        return connection;
    }

    /**
     * To store a connection in this processor object, call this setter.
     * 
     * NOTE: Calling {@link #createConnection(ConnectionContext)} does
     * <b>not</b> set this processor's connection - that method only creates the
     * connection and puts that connection in the context. It does not save that
     * connection in this processor object. You must explicitly set the
     * connection via this method if you want that connection cached here.
     * 
     * @param connection
     */
    protected void setConnection(Connection connection) {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (JMSException e) {
                log.error("Cannot close the previous connection; memory might leak.", e);
            }
        }
        this.connection = connection;
    }

    /**
     * Creates a connection using this object's connection factory and stores
     * that connection in the given context object.
     * 
     * NOTE: this does <b>not</b> set the connection in this processor object.
     * If the caller wants the created connection cached in this processor
     * object, {@link #setConnection(Connection)} must be passed the connection
     * found in the context after this method returns.
     * 
     * @param context
     *            the context where the new connection is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null
     */
    protected void createConnection(ConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        ConnectionFactory factory = getConnectionFactory();
        Connection conn = factory.createConnection();
        context.setConnection(conn);
    }

    /**
     * Creates a default session using the context's connection. This
     * implementation creates a non-transacted, auto-acknowledged session.
     * Subclasses are free to override this behavior.
     * 
     * @param context
     *            the context where the new session is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's connection is null
     */
    protected void createSession(ConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Connection conn = context.getConnection();
        if (conn == null) {
            throw new NullPointerException("The context had a null connection");
        }
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        context.setSession(session);
    }

    /**
     * Creates a destination using the context's session. The destination
     * correlates to the given named queue or topic.
     * 
     * @param context
     *            the context where the new destination is stored
     * @param endpoint
     *            identifies the queue or topic
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null or
     *             endpoint is null
     */
    protected void createDestination(ConnectionContext context, Endpoint endpoint) throws JMSException {
        if (endpoint == null) {
            throw new NullPointerException("Endpoint is null");
        }
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Destination dest;
        if (endpoint.getType() == Endpoint.Type.QUEUE) {
            dest = session.createQueue(endpoint.getName());
        } else {
            dest = session.createTopic(endpoint.getName());
        }
        context.setDestination(dest);
    }

}
