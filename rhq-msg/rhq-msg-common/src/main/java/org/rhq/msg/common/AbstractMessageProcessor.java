package org.rhq.msg.common;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;
import org.rhq.msg.common.producer.ProducerConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass that provides some functionality to connect and process messages, both as a producer or consumer. This
 * class gives you methods to create destinations, sessions, and connections. This class can cache a connection that can
 * then be used to share across multiple {@link ConnectionContext} objects. See
 * {@link #createOrReuseConnection(ConnectionContext, boolean)}.
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
     * connection via this method if you want that connection cached here. See
     * also {@link #createOrReuseConnection(ConnectionContext, boolean)}.
     * 
     * @param connection
     * 
     * @see #createOrReuseConnection(ConnectionContext, boolean)
     */
    protected void setConnection(Connection connection) {
        if (this.connection != null) {
            try {
                // make sure it is closed to free up any resources it was using
                this.connection.close();
            } catch (JMSException e) {
                log.error("Cannot close the previous connection; memory might leak.", e);
            }
        }
        this.connection = connection;
    }

    /**
     * This method provides a way to cache and share a connection across
     * multiple contexts. It combines the creation and setting of the
     * connection. This also can optionally start the connection immediately.
     * Use this if you want to reuse any connection that may already be stored
     * in this processor object (i.e. {@link #getConnection()} is non-null). If
     * there is no connection yet, one will be created. Whether the connection
     * is created or reused, that connection will be stored in the given
     * context.
     * 
     * @param context
     *            the connection will be stored in this context
     * @param start
     *            if true, the created connection will be started.
     * @throws JMSException
     */
    protected void createOrReuseConnection(ConnectionContext context, boolean start) throws JMSException {
        Connection conn = getConnection();
        if (conn != null) {
            // already have a connection cached, give it to the context
            context.setConnection(conn);
        } else {
            // there is no connection yet; create it and cache it
            createConnection(context);
            conn = context.getConnection();
            setConnection(conn);
        }

        if (start) {
            conn.start(); // calling start on started connection is ignored
        }
    }

    /**
     * Creates a connection using this object's connection factory and stores
     * that connection in the given context object.
     * 
     * NOTE: this does <b>not</b> set the connection in this processor object.
     * If the caller wants the created connection cached in this processor
     * object, {@link #setConnection(Connection)} must be passed the connection
     * found in the context after this method returns. See also
     * {@link #createOrReuseConnection(ConnectionContext, boolean).
     * 
     * @param context
     *            the context where the new connection is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null
     * 
     * @see #createOrReuseConnection(ConnectionContext, boolean)
     * @see #setConnection(Connection)
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

    /**
     * Creates a message producer using the context's session and destination.
     * 
     * @param context
     *            the context where the new producer is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null or the context's destination is null
     */
    protected void createProducer(ProducerConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Destination dest = context.getDestination();
        if (dest == null) {
            throw new NullPointerException("The context had a null destination");
        }
        MessageProducer producer = session.createProducer(dest);
        context.setMessageProducer(producer);
    }

    /**
     * Creates a text message that can be send via a producer that contains the given BasicMessage's JSON encoded data.
     * 
     * @param context
     *            the context whose session is used to create the message
     * @param basicMessage
     *            contains the data that will be JSON-encoded and encapsulated in the created message
     * @return the message that can be produced
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null
     */
    protected Message createMessage(ConnectionContext context, BasicMessage basicMessage) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Message msg = session.createTextMessage(basicMessage.toJSON());
        return msg;
    }

    /**
     * Creates a message consumer using the context's session and destination.
     * 
     * @param context
     *            the context where the new consumer is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null or the context's destination is null
     */
    protected void createConsumer(ConsumerConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Destination dest = context.getDestination();
        if (dest == null) {
            throw new NullPointerException("The context had a null destination");
        }
        MessageConsumer consumer = session.createConsumer(dest);
        context.setMessageConsumer(consumer);
    }

    /**
     * Creates a new producer connection context, reusing any existing connection that might have already been created.
     * The destination of the connection's session will be that of the given endpoint.
     * 
     * @param endpoint
     *            where the producer will send messages
     * @return the new producer connection context fully populated
     * @throws JMSException
     */
    protected ProducerConnectionContext createProducerConnectionContext(Endpoint endpoint) throws JMSException {
        ProducerConnectionContext context = new ProducerConnectionContext();
        createOrReuseConnection(context, true);
        createSession(context);
        createDestination(context, endpoint);
        createProducer(context);
        return context;
    }

    /**
     * Creates a new consumer connection context, reusing any existing connection that might have already been created.
     * The destination of the connection's session will be that of the given endpoint.
     * 
     * @param endpoint
     *            where the consumer will listen for messages
     * @return the new consumer connection context fully populated
     * @throws JMSException
     */
    protected ConsumerConnectionContext createConsumerConnectionContext(Endpoint endpoint) throws JMSException {
        ConsumerConnectionContext context = new ConsumerConnectionContext();
        createOrReuseConnection(context, true);
        createSession(context);
        createDestination(context, endpoint);
        createConsumer(context);
        return context;
    }

}
