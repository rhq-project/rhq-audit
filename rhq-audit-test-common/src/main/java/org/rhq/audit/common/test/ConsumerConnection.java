package org.rhq.audit.common.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ConsumerConnection {
    private Connection connection;

    public ConsumerConnection(String brokerURL, String queue, MessageListener messageListener) throws JMSException {
        createConnection(brokerURL, queue, messageListener);
    }

    protected void createConnection(String brokerURL, String queue, MessageListener messageListener) throws JMSException {
        ConnectionFactory connFactory = new ActiveMQConnectionFactory(brokerURL);
        Connection conn = connFactory.createConnection();
        conn.start();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = session.createQueue(queue);
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(messageListener);
        this.connection = conn;
    }

    public void close() throws JMSException {
        connection.close();
    }
}
