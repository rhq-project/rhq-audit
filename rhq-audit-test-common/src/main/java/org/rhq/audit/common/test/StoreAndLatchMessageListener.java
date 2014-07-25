package org.rhq.audit.common.test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Creates a simple test listener. Logs messages and errors it gets in lists
 * given to it. This listener will notify when it gets a message by counting
 * down a latch.
 */
public class StoreAndLatchMessageListener implements MessageListener {

    private final CountDownLatch latch;
    private final ArrayList<String> messages;
    private final ArrayList<String> errors;

    public StoreAndLatchMessageListener(CountDownLatch latch, ArrayList<String> messages, ArrayList<String> errors) {
        this.latch = latch;
        this.messages = messages;
        this.errors = errors;
    }

    public void onMessage(Message message) {
        try {
            String receivedMessage = ((TextMessage) message).getText();
            messages.add(receivedMessage);
        } catch (Exception e) {
            errors.add(e.toString());
        } finally {
            latch.countDown();
        }
    }
}