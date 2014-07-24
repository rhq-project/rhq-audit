package org.rhq.audit.producer;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

public class App {
    public static void main(String[] args) throws Exception {
        String brokerURL = "tcp://localhost:61616";
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
        Producer producer = new Producer(factory, "test");
        producer.run();
        producer.close();
    }
}
