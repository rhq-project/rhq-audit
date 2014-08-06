package org.rhq.msg.common.consumer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.rhq.msg.common.BasicMessage;
import org.rhq.msg.common.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message listener that expects to receive a JSON-encoded BasicMessage or one of its subclasses. Implementors need
 * only implement the method that takes an BasicRecord or one of its subclasses; the JSON decoding is handled for you.
 * 
 * Subclasses will typically override {@link #BasicMessageListener(Class)} or {@link #determineBasicMessageClass()}
 * unless either (a) the subclass hierarchy has generic types that are specific enough for reflection to determine the
 * type of {@link BasicMessage} being listened for or (b) the message type being listened for is {@link BasicMessage}
 * and not one of its subclasses.
 */

public abstract class BasicMessageListener<T extends BasicMessage> implements MessageListener {
    protected final Logger log = LoggerFactory.getLogger(BasicMessageListener.class);

    // In order to convert a JSON string to a BasicMessage object (or one of its subclasses), we need the actual Java
    // class of the generic type T. Java does not make it easy to find the class representation of T. This field will
    // store the actual class when we can actually determine what it is, which will be used when we decode a JSON string
    // into an instance of that class.
    private final Class<T> jsonDecoderRing;

    public BasicMessageListener() {
        this.jsonDecoderRing = determineBasicMessageClass();
    }

    /**
     * If a subclass knows the type and can give it to us, that will be the type used to decode JSON strings into that
     * message type. If this constructor is not used by subclasses, typically those subclasses will need to override
     * {@link #determineBasicMessageClass()} unless {@link BasicMessage} is the message type that subclass wants to
     * explicitly use (as opposed to a subclass of BasicMessage).
     * 
     * @param jsonDecoderRing
     *            the class representation of the generic type T
     */
    protected BasicMessageListener(Class<T> jsonDecoderRing) {
        this.jsonDecoderRing = jsonDecoderRing;
    }

    @Override
    public void onMessage(Message message) {
        T basicMessage;

        try {
            String receivedBody = ((TextMessage) message).getText();
            basicMessage = BasicMessage.fromJSON(receivedBody, getBasicMessageClass());

            // grab some headers and put them in the message
            basicMessage.setMessageId(new MessageId(message.getJMSMessageID()));
            if (message.getJMSCorrelationID() != null) {
                basicMessage.setCorrelationId(new MessageId(message.getJMSCorrelationID()));
            }

            log.trace("Received basic message: {}", basicMessage);
        } catch (JMSException e) {
            log.error("A message was received that was not a valid text message", e);
            return;
        } catch (Exception e) {
            log.error("A message was received that was not a valid JSON-encoded BasicMessage object", e);
            return;
        }

        onBasicMessage(basicMessage);
    }

    protected Class<T> getBasicMessageClass() {
        return jsonDecoderRing;
    }

    /**
     * In order to decode the JSON, we need the class representation of the basic message type. This method uses
     * reflection to try to get that type.
     * 
     * Subclasses can override this if they want to provide the class representation themselves (e.g. in case the
     * reflection cannot get it). Alternatively, subclasses can utilize the constructor
     * {@link BasicMessageListener#BasicMessageListener(Class)} to tell this object what the class of T is.
     * 
     * @return class of T
     */
    protected Class<T> determineBasicMessageClass() {
        // all of this is usually going to just return BasicMessage.class - but in case there is a subclass hierarchy
        // that makes it more specific, this will help discover the message class.
        Class<?> thisClazz = this.getClass();
        ParameterizedType parameterizedType = (ParameterizedType) thisClazz.getGenericSuperclass();
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        Class<T> clazz;
        if (actualTypeArgument instanceof Class<?>) {
            clazz = (Class<T>) actualTypeArgument;
        } else {
            TypeVariable<?> typeVar = (TypeVariable<?>) actualTypeArgument;
            clazz = (Class<T>) typeVar.getBounds()[0];
        }
        return clazz;
    }

    /**
     * Subclasses implement this method to process the received message.
     * 
     * @param message
     *            the message to process
     */
    protected abstract void onBasicMessage(T basicMessage);
}
