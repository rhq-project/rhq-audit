package org.rhq.audit.broker;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class EmbeddedBrokerTest {
    public void testInternalVMBroker() throws Exception {
        EmbeddedBroker broker = null;
        broker = new EmbeddedBroker(new String[] { "--config=simple-activemq.properties" });
        broker.startBroker();
        internalTestBroker(broker, "vm://simple-testbroker?create=false");
    }

    public void testTCPBroker() throws Exception {
        EmbeddedBroker broker = null;
        int bindPort = findFreePort();
        broker = new EmbeddedBroker(new String[] { "--config=simple-activemq.xml", "-Dtest.bind.port=" + bindPort });
        broker.startBroker();
        internalTestBroker(broker, "tcp://localhost:" + bindPort);
    }

    private void internalTestBroker(EmbeddedBroker broker, String brokerURL) throws Exception {
        // test that messages can flow to the given broker
        try {
            CountDownLatch latch = new CountDownLatch(1);
            String testMessage = "Hello World!";
            ArrayList<String> receivedMessages = new ArrayList<String>();
            ArrayList<String> errors = new ArrayList<String>();

            // start the consumer
            ConnectionFactory consumerConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
            Connection consumerConnection = consumerConnectionFactory.createConnection();
            consumerConnection.start();
            Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination consumerDestination = consumerSession.createQueue("test");
            MessageConsumer consumer = consumerSession.createConsumer(consumerDestination);
            consumer.setMessageListener(new TestMessageListener(latch, receivedMessages, errors));

            // start the producer
            ConnectionFactory producerFactory = new ActiveMQConnectionFactory(brokerURL);
            Connection producerConnection = producerFactory.createConnection();
            producerConnection.start();
            Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination producerDestination = producerSession.createQueue("test");
            MessageProducer producer = consumerSession.createProducer(producerDestination);
            Message producerMessage = producerSession.createTextMessage(testMessage);
            producer.send(producerMessage);

            // wait for the message to flow
            boolean gotMessage = latch.await(5, TimeUnit.SECONDS);
            if (!gotMessage) {
                errors.add("Timed out waiting for message - it never showed up");
            }

            // close everything
            producerConnection.close();
            consumerConnection.close();

            // make sure the message flowed properly
            Assert.assertTrue(errors.isEmpty(), "Failed to send message propertly: " + errors);
            Assert.assertFalse(receivedMessages.isEmpty(), "Didn't receive message");
            Assert.assertEquals(receivedMessages.get(0), testMessage);

        } finally {
            broker.stopBroker();
        }
    }

    private int findFreePort() throws Exception {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(0);
            return ss.getLocalPort();
        } finally {
            if (ss != null) {
                ss.close();
            }
        }

    }
}
